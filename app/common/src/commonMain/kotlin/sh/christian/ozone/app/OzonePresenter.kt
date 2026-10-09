package sh.christian.ozone.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlin.time.Clock
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.compose.ComposePostPresenter
import sh.christian.ozone.compose.ComposePostProps
import sh.christian.ozone.home.HomeScreen
import sh.christian.ozone.home.SelectedHomeScreenTab
import sh.christian.ozone.login.LoginPresenter
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.model.Moment
import sh.christian.ozone.model.Notifications
import sh.christian.ozone.notifications.NotificationsRepository
import sh.christian.ozone.notifications.NotificationsScreen
import sh.christian.ozone.profile.ProfilePresenter
import sh.christian.ozone.profile.ProfileProps
import sh.christian.ozone.settings.SettingsScreen
import sh.christian.ozone.thread.ThreadPresenter
import sh.christian.ozone.thread.ThreadProps
import sh.christian.ozone.timeline.TimelineRepository
import sh.christian.ozone.timeline.TimelineScreen
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.user.MyProfileRepository
import software.ralf.app.platform.presenter.compose.ComposePresenter

/** The root presenter owns the explicit in-app navigation stack. */
@Inject
class OzonePresenter(
  private val loginRepository: LoginRepository,
  private val apiProvider: ApiProvider,
  private val loginPresenter: LoginPresenter,
  private val timelineRepository: TimelineRepository,
  private val notificationsRepository: NotificationsRepository,
  private val myProfileRepository: MyProfileRepository,
  private val profilePresenter: ProfilePresenter,
  private val threadPresenter: ThreadPresenter,
  private val composePostPresenter: ComposePostPresenter,
  private val clock: Clock,
) : ComposePresenter<Unit, OzoneTemplate> {
  @Composable
  override fun present(input: Unit): OzoneTemplate {
    var routes by remember { mutableStateOf(listOf<AppDestination>(AppDestination.Home)) }
    var imageModal by remember { mutableStateOf<ImageModalModel?>(null) }
    val auth by loginRepository.authFlow().collectAsState(loginRepository.auth)
    if (auth == null) {
      return loginPresenter.present(
        LoginPresenter.Input(
          onLoggedIn = {
            routes = listOf(AppDestination.Home)
            loginRepository.auth = it
          },
          onCanceled = {},
        )
      )
    }

    fun push(route: AppDestination) {
      routes = routes + route
    }
    fun pop() {
      if (routes.size > 1) routes = routes.dropLast(1)
    }

    val template = when (val route = routes.last()) {
      AppDestination.Home -> presentHome(
        onProfile = { push(AppDestination.Profile(it)) },
        onThread = { push(AppDestination.Thread(it)) },
        onCompose = { push(AppDestination.Compose(it)) },
        onOpenImage = { action -> imageModal = ImageModalModel(action) { imageModal = null } },
      )
      is AppDestination.Profile -> profilePresenter.present(
        ProfilePresenter.Input(
          props = route.props,
          onExit = ::pop,
          onOpenPost = { push(AppDestination.Thread(it)) },
          onOpenUser = { push(AppDestination.Profile(ProfileProps(it))) },
          onOpenImage = { action -> imageModal = ImageModalModel(action) { imageModal = null } },
          onReplyToPost = { push(AppDestination.Compose(ComposePostProps(it))) },
        )
      )
      is AppDestination.Thread -> threadPresenter.present(
        ThreadPresenter.Input(
          props = route.props,
          onExit = ::pop,
          onOpenPost = { push(AppDestination.Thread(it)) },
          onOpenUser = { push(AppDestination.Profile(ProfileProps(it))) },
          onOpenImage = { action -> imageModal = ImageModalModel(action) { imageModal = null } },
          onReplyToPost = { push(AppDestination.Compose(ComposePostProps(it))) },
        )
      )
      is AppDestination.Compose -> composePostPresenter.present(
        ComposePostPresenter.Input(
          props = route.props,
          onExit = ::pop,
          onCreated = ::pop,
        )
      )
    }
    val content = when (template) {
      is OzoneTemplate.FullScreen -> template.content
      is OzoneTemplate.Modal -> template.content
    }
    return imageModal?.let { OzoneTemplate.Modal(content, it) } ?: template
  }

  @Composable
  private fun presentHome(
    onProfile: (ProfileProps) -> Unit,
    onThread: (ThreadProps) -> Unit,
    onCompose: (ComposePostProps) -> Unit,
    onOpenImage: (OpenImageAction) -> Unit,
  ): OzoneTemplate {
    var tab by remember { mutableStateOf(SelectedHomeScreenTab.TIMELINE) }
    val scope = rememberCoroutineScope()
    val profile by myProfileRepository.me().collectAsState()
    val timeline by timelineRepository.timeline.collectAsState(null)
    val notifications by notificationsRepository.notifications.collectAsState(
      Notifications(persistentListOf(), null)
    )
    val unread by notificationsRepository.unreadCount.collectAsState(0)

    val child = when (tab) {
      SelectedHomeScreenTab.TIMELINE -> TimelineScreen(
        now = Moment(clock.now()),
        profile = profile,
        timeline = timeline?.posts ?: persistentListOf(),
        showRefreshPrompt = false,
        showComposePostButton = true,
        onRefresh = { scope.launch { timelineRepository.refresh() } },
        onLoadMore = { scope.launch { timelineRepository.loadMore() } },
        onComposePost = { onCompose(ComposePostProps()) },
        onOpenPost = onThread,
        onOpenUser = { user -> onProfile(ProfileProps(user, profile?.takeIf { myProfileRepository.isMe(user) })) },
        onOpenImage = onOpenImage,
        onReplyToPost = { onCompose(ComposePostProps(it)) },
        onExit = {},
      )
      SelectedHomeScreenTab.NOTIFICATIONS -> NotificationsScreen(
        now = Moment(clock.now()),
        notifications = notifications.list,
        onLoadMore = { scope.launch { notificationsRepository.loadMore() } },
        onExit = {},
        onOpenPost = onThread,
        onOpenUser = { onProfile(ProfileProps(it)) },
        onOpenImage = onOpenImage,
        onReplyToPost = { onCompose(ComposePostProps(it)) },
      )
      SelectedHomeScreenTab.SETTINGS -> SettingsScreen(
        onExit = {},
        onSignOut = apiProvider::signOut,
      )
    }
    return OzoneTemplate.FullScreen(
      HomeScreen(
        homeContent = persistentListOf(child),
        unreadCount = unread.takeIf { it > 0 }?.toString(),
        tab = tab,
        onChangeTab = { tab = it },
        onExit = {},
      )
    )
  }
}

private sealed interface AppDestination {
  data object Home : AppDestination
  data class Profile(val props: ProfileProps) : AppDestination
  data class Thread(val props: ThreadProps) : AppDestination
  data class Compose(val props: ComposePostProps) : AppDestination
}
