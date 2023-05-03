package sh.christian.ozone.compose

import app.bsky.feed.Post
import app.bsky.richtext.Facet
import app.bsky.richtext.FacetByteSlice
import app.bsky.richtext.FacetFeatureUnion.Link
import app.bsky.richtext.FacetFeatureUnion.Mention
import app.bsky.richtext.FacetLink
import app.bsky.richtext.FacetMention
import com.atproto.repo.CreateRecordRequest
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
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
import sh.christian.ozone.model.LinkTarget.ExternalLink
import sh.christian.ozone.model.LinkTarget.UserMention
import sh.christian.ozone.model.Profile
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.user.UserDatabase
import sh.christian.ozone.user.UserReference
import sh.christian.ozone.util.serialize

class ComposePostWorkflow(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val userDatabase: UserDatabase,
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
    val resolvedLinks = coroutineScope {
      post.links.map { link ->
        async {
          when (link.target) {
            is ExternalLink -> link
            is UserMention -> {
              userDatabase.profileOrNull(UserReference.Handle(link.target.did))
                .first()
                ?.let { profile -> link.copy(target = UserMention(profile.did)) }
            }
          }
        }
      }.awaitAll()
    }.filterNotNull()

    val request = CreateRecordRequest(
      repo = post.authorDid,
      collection = "app.bsky.feed.post",
      record = Post.serializer().serialize(
        Post(
          text = post.text,
          facets = resolvedLinks.map { link ->
            Facet(
              index = FacetByteSlice(link.start.toLong(), link.end.toLong()),
              features = listOf(
                when (link.target) {
                  is ExternalLink -> Link(FacetLink(link.target.url))
                  is UserMention -> Mention(FacetMention(link.target.did))
                }
              ),
            )
          },
          createdAt = clock.now(),
        )
      ),
    )

    apiProvider.api.createRecord(request)
  }
}
