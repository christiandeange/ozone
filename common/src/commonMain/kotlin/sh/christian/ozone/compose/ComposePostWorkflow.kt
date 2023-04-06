package sh.christian.ozone.compose

import app.bsky.feed.Post
import com.atproto.repo.CreateRecordRequest
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import kotlinx.datetime.Clock
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.NetworkWorker
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.compose.ComposePostOutput.CanceledPost
import sh.christian.ozone.compose.ComposePostOutput.CreatedPost
import sh.christian.ozone.compose.ComposePostState.ComposingPost
import sh.christian.ozone.compose.ComposePostState.CreatingPost
import sh.christian.ozone.compose.ComposePostState.ShowingError
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorProps.CustomError
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.model.Profile
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.util.serialize

class ComposePostWorkflow(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<ComposePostProps, ComposePostState, ComposePostOutput, AppScreen>() {
  override fun initialState(
    props: ComposePostProps,
    snapshot: Snapshot?
  ): ComposePostState = ComposingPost

  override fun render(
    renderProps: ComposePostProps,
    renderState: ComposePostState,
    context: RenderContext
  ): AppScreen = when (renderState) {
    is ComposingPost -> {
      AppScreen(main = context.composePostScreen(renderProps.profile))
    }
    is CreatingPost -> {
      context.runningWorker(post(renderState.postPayload)) { result ->
        action {
          when (result) {
            is AtpResponse.Success -> {
              setOutput(CreatedPost)
            }
            is AtpResponse.Failure -> {
              val errorProps = result.toErrorProps(true)
                ?: CustomError("Oops.", "Something bad happened.", false)

              state = ShowingError(errorProps, renderState.postPayload)
            }
          }
        }
      }

      AppScreen(
        main = context.composePostScreen(renderProps.profile),
        overlay = TextOverlayScreen(
          onDismiss = Dismissable.Ignore,
          text = "Posting...",
        )
      )
    }
    is ShowingError -> {
      AppScreen(
        main = context.composePostScreen(renderProps.profile),
        overlay = context.renderChild(errorWorkflow, renderState.errorProps) { output ->
          action {
            state = when (output) {
              ErrorOutput.Dismiss -> ComposingPost
              ErrorOutput.Retry -> CreatingPost(renderState.postPayload)
            }
          }
        }
      )
    }
  }

  override fun snapshotState(state: ComposePostState): Snapshot? = null

  private fun RenderContext.composePostScreen(profile: Profile): ComposePostScreen {
    return ComposePostScreen(
      profile = profile,
      onExit = eventHandler {
        setOutput(CanceledPost)
      },
      onPost = eventHandler { payload ->
        state = CreatingPost(payload)
      },
    )
  }

  private fun post(post: PostPayload) = NetworkWorker {
    val request = CreateRecordRequest(
      repo = post.authorDid,
      collection = "app.bsky.feed.post",
      record = Post.serializer().serialize(
        Post(
          text = post.text,
          createdAt = clock.now().toString(),
        )
      ),
    )

    apiProvider.api.createRecord(request)
  }
}
