package sh.christian.ozone.thread

import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponseThreadUnion.BlockedPost
import app.bsky.feed.GetPostThreadResponseThreadUnion.NotFoundPost
import app.bsky.feed.GetPostThreadResponseThreadUnion.ThreadViewPost
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import kotlinx.datetime.Clock
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.NetworkWorker
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.model.Thread
import sh.christian.ozone.model.toThread
import sh.christian.ozone.profile.ProfileProps
import sh.christian.ozone.profile.ProfileWorkflow
import sh.christian.ozone.thread.ThreadState.FetchingPost
import sh.christian.ozone.thread.ThreadState.ShowingError
import sh.christian.ozone.thread.ThreadState.ShowingFullSizeImage
import sh.christian.ozone.thread.ThreadState.ShowingPost
import sh.christian.ozone.thread.ThreadState.ShowingProfile
import sh.christian.ozone.ui.compose.ImageOverlayScreen
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.EmptyScreen

class ThreadWorkflow(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val profileWorkflow: () -> ProfileWorkflow,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<ThreadProps, ThreadState, Unit, AppScreen>() {
  override fun initialState(
    props: ThreadProps,
    snapshot: Snapshot?,
  ): ThreadState {
    return FetchingPost(
      thread = props.originalPost?.let { originalPost ->
        Thread(
          post = originalPost,
          parents = emptyList(),
          replies = emptyList(),
        )
      },
      previousState = null,
      uri = props.uri,
    )
  }

  override fun render(
    renderProps: ThreadProps,
    renderState: ThreadState,
    context: RenderContext
  ): AppScreen {
    val screenStack = generateSequence(renderState) { it.previousState }
      .filter { it.thread != null }
      .toList()
      .reversed()
      .map { state -> context.threadScreen(state.thread!!) }
      .ifEmpty { listOf(EmptyScreen) }

    return when (renderState) {
      is FetchingPost -> {
        context.runningWorker(loadPost(renderState.uri)) { result ->
          action {
            state = when (result) {
              is AtpResponse.Success -> {
                when (val thread = result.response.thread) {
                  is ThreadViewPost -> ShowingPost(
                    previousState = state.previousState,
                    thread = thread.value.toThread(),
                  )
                  is NotFoundPost,
                  is BlockedPost -> ShowingError(
                    previousState = state,
                    props = ErrorProps.CustomError("Oops.", "Could not load thread.", false),
                  )
                }
              }
              is AtpResponse.Failure -> {
                val errorProps = result.toErrorProps(true)
                  ?: ErrorProps.CustomError("Oops.", "Could not load thread.", false)

                ShowingError(
                  previousState = state,
                  props = errorProps,
                )
              }
            }
          }
        }

        AppScreen(
          mains = screenStack,
          overlay = TextOverlayScreen(
            onDismiss = Dismissable.Ignore,
            text = "Loading thread...",
          ),
        )
      }
      is ShowingPost -> AppScreen(mains = screenStack)
      is ShowingProfile -> {
        val profileScreen = context.renderChild(profileWorkflow(), renderState.props) {
          action {
            state = renderState.previousState
          }
        }

        profileScreen.copy(mains = screenStack + profileScreen.mains)
      }
      is ShowingFullSizeImage -> {
        AppScreen(
          mains = screenStack,
          overlay = ImageOverlayScreen(
            onDismiss = Dismissable.DismissHandler(
              context.eventHandler { state = renderState.previousState }
            ),
            action = renderState.openImageAction,
          ),
        )
      }
      is ShowingError -> {
        AppScreen(
          mains = screenStack,
          overlay = context.renderChild(errorWorkflow, renderState.props) { output ->
            action {
              when (output) {
                ErrorOutput.Dismiss -> setOutput(Unit)
                ErrorOutput.Retry -> state = renderState.previousState
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: ThreadState): Snapshot? = null

  private fun RenderContext.threadScreen(thread: Thread): ThreadScreen {
    return ThreadScreen(
      now = clock.now(),
      thread = thread,
      onExit = eventHandler {
        state.previousState
          ?.let { state = it }
          ?: setOutput(Unit)
      },
      onRefresh = eventHandler {
        state = FetchingPost(
          previousState = state.previousState,
          thread = thread,
          uri = thread.post.uri,
        )
      },
      onOpenPost = eventHandler { post ->
        state = FetchingPost(
          thread = post.originalPost?.let { originalPost ->
            Thread(
              post = originalPost,
              parents = emptyList(),
              replies = emptyList(),
            )
          },
          previousState = state,
          uri = post.uri,
        )
      },
      onOpenImage = eventHandler { action ->
        state = ShowingFullSizeImage(state, action)
      },
      onOpenUser = eventHandler { user ->
        state = ShowingProfile(state, ProfileProps(user, null))
      }
    )
  }

  private fun loadPost(uri: String) = NetworkWorker {
    apiProvider.api.getPostThread(GetPostThreadQueryParams(uri))
  }
}
