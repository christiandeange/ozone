package sh.christian.ozone.thread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponseThreadUnion.ThreadViewPost
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
import sh.christian.ozone.model.Thread
import sh.christian.ozone.model.toThread
import sh.christian.ozone.ui.compose.OpenImageAction
import software.ralf.app.platform.presenter.compose.ComposePresenter

@Inject
class ThreadPresenter(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val errorPresenter: ErrorPresenter,
) : ComposePresenter<ThreadPresenter.Input, OzoneTemplate> {
  @Composable
  override fun present(input: Input): OzoneTemplate {
    var thread by remember(input.props.uri) { mutableStateOf<Thread?>(input.props.originalPost?.let {
      Thread(it, kotlinx.collections.immutable.persistentListOf(), kotlinx.collections.immutable.persistentListOf())
    }) }
    var error by remember(input.props.uri) { mutableStateOf<ErrorProps?>(null) }
    var refreshVersion by remember(input.props.uri) { mutableStateOf(0) }

    suspend fun refresh() {
      when (val response = apiProvider.api.getPostThread(GetPostThreadQueryParams(input.props.uri))) {
        is AtpResponse.Success -> {
          thread = (response.response.thread as? ThreadViewPost)?.value?.toThread()
          if (thread == null) error = ErrorProps("Oops.", "Could not load thread.", false)
        }
        is AtpResponse.Failure -> error = response.toErrorProps(true)
          ?: ErrorProps("Oops.", "Could not load thread.", true)
      }
    }

    LaunchedEffect(input.props.uri, refreshVersion) { refresh() }
    val loadedThread = thread ?: return OzoneTemplate.FullScreen(LoadingScreen("Loading thread…"))
    val screen = ThreadScreen(
      now = Moment(clock.now()),
      thread = loadedThread,
      onExit = input.onExit,
      onRefresh = {
        thread = null
        refreshVersion++
      },
      onOpenPost = input.onOpenPost,
      onOpenUser = input.onOpenUser,
      onOpenImage = input.onOpenImage,
      onReplyToPost = input.onReplyToPost,
    )
    val errorProps = error ?: return OzoneTemplate.FullScreen(screen)
    return OzoneTemplate.Modal(
      content = screen,
      modal = errorPresenter.present(ErrorPresenter.Input(errorProps) { output ->
        when (output) {
          ErrorOutput.Dismiss -> error = null
          ErrorOutput.Retry -> {
            error = null
            thread = null
            refreshVersion++
          }
        }
      }),
    )
  }

  data class Input(
    val props: ThreadProps,
    val onExit: () -> Unit,
    val onOpenPost: (ThreadProps) -> Unit,
    val onOpenUser: (sh.christian.ozone.user.UserReference) -> Unit,
    val onOpenImage: (OpenImageAction) -> Unit,
    val onReplyToPost: (sh.christian.ozone.compose.PostReplyInfo) -> Unit,
  )
}
