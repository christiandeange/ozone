package sh.christian.ozone.compose

import app.bsky.feed.Post
import app.bsky.feed.PostReplyRef
import app.bsky.richtext.Facet
import app.bsky.richtext.FacetByteSlice
import app.bsky.richtext.FacetFeatureUnion.Link
import app.bsky.richtext.FacetFeatureUnion.Mention
import app.bsky.richtext.FacetFeatureUnion.Tag
import app.bsky.richtext.FacetLink
import app.bsky.richtext.FacetMention
import app.bsky.richtext.FacetTag
import com.atproto.repo.CreateRecordRequest
import com.atproto.repo.CreateRecordResponse
import com.atproto.repo.StrongRef
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.AtIdentifier
import sh.christian.ozone.api.NetworkWorker
import sh.christian.ozone.api.Nsid
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.compose.ComposePostOutput.CanceledPost
import sh.christian.ozone.compose.ComposePostOutput.CreatedPost
import sh.christian.ozone.compose.ComposePostState.ComposingPost
import sh.christian.ozone.compose.ComposePostState.CreatingPost
import sh.christian.ozone.compose.ComposePostState.ShowingError
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.model.LinkTarget.ExternalLink
import sh.christian.ozone.model.LinkTarget.Hashtag
import sh.christian.ozone.model.LinkTarget.UserDidMention
import sh.christian.ozone.model.LinkTarget.UserHandleMention
import sh.christian.ozone.model.Profile
import sh.christian.ozone.model.TimelinePostLink
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.user.MyProfileRepository
import sh.christian.ozone.user.UserDatabase
import sh.christian.ozone.user.UserHandle
import sh.christian.ozone.util.serialize

@Inject
class ComposePostWorkflow(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val userDatabase: UserDatabase,
  private val myProfileRepository: MyProfileRepository,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<ComposePostProps, ComposePostState, ComposePostOutput, AppScreen>() {
  override fun initialState(
    props: ComposePostProps,
    snapshot: Snapshot?
  ): ComposePostState = ComposingPost(myProfileRepository.me().value!!)

  override fun render(
    renderProps: ComposePostProps,
    renderState: ComposePostState,
    context: RenderContext
  ): AppScreen {
    val myProfileWorker = myProfileRepository.me().filterNotNull().asWorker()
    context.runningWorker(myProfileWorker) { profile ->
      action {
        state = when (val currentState = state) {
          is ComposingPost -> currentState.copy(myProfile = profile)
          is CreatingPost -> currentState.copy(myProfile = profile)
          is ShowingError -> currentState.copy(myProfile = profile)
        }
      }
    }

    return when (renderState) {
      is ComposingPost -> {
        AppScreen(main = context.composePostScreen(renderState.myProfile, renderProps.replyTo))
      }

      is CreatingPost -> {
        context.runningWorker(post(renderState.postPayload, renderProps.replyTo)) { result ->
          action {
            when (result) {
              is AtpResponse.Success -> {
                setOutput(CreatedPost)
              }

              is AtpResponse.Failure -> {
                val errorProps = result.toErrorProps(true)
                  ?: ErrorProps("Oops.", "Something bad happened.", false)

                state = ShowingError(state.myProfile, errorProps, renderState.postPayload)
              }
            }
          }
        }

        AppScreen(
          main = context.composePostScreen(renderState.myProfile, renderProps.replyTo),
          overlay = TextOverlayScreen(
            onDismiss = Dismissable.Ignore,
            text = "Posting...",
          )
        )
      }

      is ShowingError -> {
        AppScreen(
          main = context.composePostScreen(renderState.myProfile, renderProps.replyTo),
          overlay = context.renderChild(errorWorkflow, renderState.errorProps) { output ->
            action {
              state = when (output) {
                ErrorOutput.Dismiss -> ComposingPost(state.myProfile)
                ErrorOutput.Retry -> CreatingPost(state.myProfile, renderState.postPayload)
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: ComposePostState): Snapshot? = null

  private fun RenderContext.composePostScreen(
    profile: Profile,
    replyInfo: PostReplyInfo?,
  ): ComposePostScreen {
    return ComposePostScreen(
      profile = profile,
      replyingTo = replyInfo?.parentAuthor,
      onExit = eventHandler {
        setOutput(CanceledPost)
      },
      onPost = eventHandler { payload ->
        state = CreatingPost(state.myProfile, payload)
      },
    )
  }

  private fun post(
    post: PostPayload,
    originalPost: PostReplyInfo?,
  ): NetworkWorker<CreateRecordResponse> = NetworkWorker {
    val resolvedLinks: List<TimelinePostLink> = coroutineScope {
      post.links.map { link ->
        async {
          when (link.target) {
            is ExternalLink -> link
            is UserDidMention -> link
            is Hashtag -> link
            is UserHandleMention -> {
              userDatabase.profileOrNull(UserHandle(link.target.handle))
                .first()
                ?.let { profile -> link.copy(target = UserDidMention(profile.did)) }
            }
          }
        }
      }.awaitAll()
    }.filterNotNull()

    val reply = originalPost?.let { original ->
      PostReplyRef(
        root = StrongRef(original.root.uri, original.root.cid),
        parent = StrongRef(original.parent.uri, original.parent.cid),
      )
    }

    val request = CreateRecordRequest(
      repo = AtIdentifier(post.authorDid.did),
      collection = Nsid("app.bsky.feed.post"),
      record = Post.serializer().serialize(
        Post(
          text = post.text,
          reply = reply,
          facets = resolvedLinks.map { link ->
            Facet(
              index = FacetByteSlice(link.start.toLong(), link.end.toLong()),
              features = when (link.target) {
                is ExternalLink -> persistentListOf(Link(FacetLink(link.target.uri)))
                is UserDidMention -> persistentListOf(Mention(FacetMention(link.target.did)))
                is Hashtag -> persistentListOf(Tag(FacetTag(link.target.tag)))
                is UserHandleMention -> persistentListOf()
              },
            )
          }.toImmutableList(),
          createdAt = clock.now(),
        )
      ),
    )

    apiProvider.api.createRecord(request)
  }
}
