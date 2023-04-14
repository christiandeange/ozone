package sh.christian.ozone.notifications

import app.bsky.notification.ListNotificationsQueryParams
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
import kotlinx.coroutines.isActive
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.model.Notifications
import sh.christian.ozone.model.toNotification
import kotlin.time.Duration.Companion.minutes

class NotificationsRepository(
  private val apiProvider: ApiProvider,
) : Supervisor {
  private val latest: MutableStateFlow<Notifications> = MutableStateFlow(EMPTY_VALUE)
  private val loadErrors: MutableSharedFlow<ErrorProps> = MutableSharedFlow()

  val notifications: Flow<Notifications> = latest
  val unreadCount: Flow<Int> = latest.map { it.list.count { notification -> !notification.isRead } }
  val errors: Flow<ErrorProps> = loadErrors

  @OptIn(ExperimentalCoroutinesApi::class)
  override suspend fun CoroutineScope.onStart() {
    apiProvider.auth()
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
      .collect { refresh() }
  }

  suspend fun refresh() {
    load(null)
  }

  suspend fun loadMore() {
    load(latest.value.cursor)
  }

  private suspend fun load(cursor: String?) {
    val response: AtpResponse<Notifications> = apiProvider.api
      .listNotifications(ListNotificationsQueryParams(limit = 100, cursor = cursor))
      .map { it -> Notifications(it.notifications.map { it.toNotification() }, it.cursor) }

    when (response) {
      is AtpResponse.Success -> {
        val newNotifications = response.response
        val mergedNotifications = if (cursor != null) {
          Notifications(
            list = latest.value.list + newNotifications.list,
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
            ?: ErrorProps.CustomError("Oops.", "Could not load notifications", true)
        )
      }
    }
  }

  private companion object {
    private val EMPTY_VALUE = Notifications(listOf(), null)
  }
}
