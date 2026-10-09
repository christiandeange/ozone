package sh.christian.ozone.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.bsky.feed.GetAuthorFeedQueryParams
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlin.time.Clock
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.LoadingScreen
import sh.christian.ozone.app.OzoneTemplate
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorPresenter
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.model.Moment
import sh.christian.ozone.model.Timeline
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.user.MyProfileRepository
import sh.christian.ozone.user.UserDatabase
import sh.christian.ozone.user.UserDid
import sh.christian.ozone.user.UserHandle
import sh.christian.ozone.util.ReadOnlyList
import sh.christian.ozone.util.toReadOnlyList
import sh.christian.ozone.ui.compose.OpenImageAction
import software.ralf.app.platform.presenter.compose.ComposePresenter

@Inject
class ProfilePresenter(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val userDatabase: UserDatabase,
  private val myProfileRepository: MyProfileRepository,
  private val errorPresenter: ErrorPresenter,
) : ComposePresenter<ProfilePresenter.Input, OzoneTemplate> {
  @Composable
  override fun present(input: Input): OzoneTemplate {
    val profile by userDatabase.profileOrNull(input.props.user).collectAsState(input.props.preloadedProfile)
    var feed by remember(input.props.user) { mutableStateOf<ReadOnlyList<TimelinePost>>(persistentListOf()) }
    var cursor by remember(input.props.user) { mutableStateOf<String?>(null) }
    var error by remember(input.props.user) { mutableStateOf<ErrorProps?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun loadMore() {
      when (val response = apiProvider.api.getAuthorFeed(
        GetAuthorFeedQueryParams(
          actor = when (val user = input.props.user) {
            is UserDid -> user.did
            is UserHandle -> user.handle
          },
          limit = 100,
          cursor = cursor,
        )
      )) {
        is AtpResponse.Success -> {
          val timeline = Timeline.from(response.response.feed, response.response.cursor)
          feed = (feed + timeline.posts).toReadOnlyList()
          cursor = timeline.cursor
        }
        is AtpResponse.Failure -> error = response.toErrorProps(true)
          ?: ErrorProps("Oops.", "Could not load this profile.", true)
      }
    }

    LaunchedEffect(input.props.user) { loadMore() }
    val loadedProfile = profile ?: return OzoneTemplate.FullScreen(LoadingScreen("Loading profile…"))
    val screen = ProfileScreen(
      now = Moment(clock.now()),
      profile = loadedProfile,
      feed = feed,
      isSelf = myProfileRepository.isMe(UserDid(loadedProfile.did)),
      onLoadMore = { scope.launch { loadMore() } },
      onOpenPost = input.onOpenPost,
      onOpenUser = input.onOpenUser,
      onOpenImage = input.onOpenImage,
      onReplyToPost = input.onReplyToPost,
      onExit = input.onExit,
    )
    val errorProps = error ?: return OzoneTemplate.FullScreen(screen)
    return OzoneTemplate.Modal(
      content = screen,
      modal = errorPresenter.present(ErrorPresenter.Input(errorProps) { output ->
        when (output) {
          ErrorOutput.Dismiss -> error = null
          ErrorOutput.Retry -> {
            error = null
            scope.launch { loadMore() }
          }
        }
      }),
    )
  }

  data class Input(
    val props: ProfileProps,
    val onExit: () -> Unit,
    val onOpenPost: (sh.christian.ozone.thread.ThreadProps) -> Unit,
    val onOpenUser: (sh.christian.ozone.user.UserReference) -> Unit,
    val onOpenImage: (OpenImageAction) -> Unit,
    val onReplyToPost: (sh.christian.ozone.compose.PostReplyInfo) -> Unit,
  )
}
