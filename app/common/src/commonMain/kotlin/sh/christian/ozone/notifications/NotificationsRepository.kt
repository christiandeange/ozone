package sh.christian.ozone.notifications

import app.bsky.feed.GetPostsQueryParams
import app.bsky.notification.ListNotificationsNotification
import app.bsky.notification.ListNotificationsQueryParams
import app.bsky.notification.ListNotificationsReason
import app.bsky.notification.ListNotificationsResponse
import app.bsky.notification.UpdateSeenRequest
import kotlinx.atomicfu.atomic
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.di.SingleInApp
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.model.Notifications
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.model.toNotification
import sh.christian.ozone.model.toPost
import sh.christian.ozone.util.mapNotNullImmutable
import sh.christian.ozone.util.toReadOnlyList
import kotlin.time.Duration.Companion.minutes

@Inject
@SingleInApp
class NotificationsRepository(
  private val apiProvider: ApiProvider,
  private val loginRepository: LoginRepository,
) : Supervisor() {
  private val latest: MutableStateFlow<Notifications> = MutableStateFlow(EMPTY_VALUE)
  private val loadErrors: MutableSharedFlow<ErrorProps> = MutableSharedFlow()
  private val onUpdateSeen: MutableSharedFlow<Unit> = MutableSharedFlow()
  private val doNotRefreshInstances = atomic(0)

  val notifications: Flow<Notifications> = latest
  val errors: Flow<ErrorProps> = loadErrors

  val unreadCount: Flow<Int> = merge(
    latest.map { it.list.count { notification -> !notification.isRead } },
    onUpdateSeen.map { 0 },
  )

  @OptIn(ExperimentalCoroutinesApi::class)
  override suspend fun CoroutineScope.onStart() {
    loginRepository.authFlow()
      .flatMapLatest { auth ->
        if (auth != null) {
          flow {
            while (currentCoroutineContext().isActive) {
              emit(Unit)
              delay(1.minutes)
            }
          }
        } else {
          latest.value = EMPTY_VALUE
          emptyFlow()
        }
      }
      .collect {
        if (doNotRefreshInstances.value == 0) {
          refresh()
        }
      }
  }

  suspend fun refresh() {
    load(null)
  }

  suspend fun loadMore() {
    load(latest.value.cursor)
  }

  suspend fun doNotRefreshWhileActive() {
    try {
      doNotRefreshInstances.incrementAndGet()

      // Hang forever, or until this coroutine is cancelled.
      suspendCancellableCoroutine<Nothing> { }
    } finally {
      doNotRefreshInstances.decrementAndGet()
    }
  }

  suspend fun updateSeenAt(time: Instant) {
    apiProvider.api.updateSeen(UpdateSeenRequest(time)).requireResponse()
    onUpdateSeen.emit(Unit)
  }

  private suspend fun load(cursor: String?) {
    val response: AtpResponse<ListNotificationsResponse> = apiProvider.api
      .listNotifications(ListNotificationsQueryParams(limit = 25, cursor = cursor))

    when (response) {
      is AtpResponse.Success -> {
        val posts = fetchPosts(response.response).associateBy { it.uri }

        val newNotifications = Notifications(
          list = response.response.notifications.mapNotNullImmutable { it.toNotification(posts) },
          cursor = response.response.cursor,
        )

        val mergedNotifications = if (cursor != null) {
          Notifications(
            list = (latest.value.list + newNotifications.list).toReadOnlyList(),
            cursor = newNotifications.cursor,
          )
        } else {
          newNotifications
        }

        latest.value = mergedNotifications
      }
      is AtpResponse.Failure -> {
        loadErrors.emit(
          response.toErrorProps(true)
            ?: ErrorProps("Oops.", "Could not load notifications", true)
        )
      }
    }
  }

  private suspend fun fetchPosts(response: ListNotificationsResponse): List<TimelinePost> {
    val postUris = response.notifications
      .mapNotNull { it.getPostUri() }
      .distinct()
      .toReadOnlyList()

    return if (postUris.isEmpty()) {
      emptyList()
    } else {
      apiProvider.api
        .getPosts(GetPostsQueryParams(postUris))
        .requireResponse()
        .posts
        .map { it.toPost() }
    }
  }

  companion object {
    private val EMPTY_VALUE = Notifications(persistentListOf(), null)

    fun ListNotificationsNotification.getPostUri(): AtUri? = when (reason) {
      is ListNotificationsReason.Unknown -> null
      is ListNotificationsReason.Like -> reasonSubject
      is ListNotificationsReason.Repost -> reasonSubject
      is ListNotificationsReason.Mention -> uri
      is ListNotificationsReason.Reply -> uri
      is ListNotificationsReason.Quote -> uri
      is ListNotificationsReason.Follow -> null
      is ListNotificationsReason.StarterpackJoined -> null
    }
  }
}
