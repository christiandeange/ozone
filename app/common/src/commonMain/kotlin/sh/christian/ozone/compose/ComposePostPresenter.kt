package sh.christian.ozone.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.atproto.repo.StrongRef
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Clock
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.Nsid
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.LoadingScreen
import sh.christian.ozone.app.OzoneTemplate
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorPresenter
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.model.LinkTarget.ExternalLink
import sh.christian.ozone.model.LinkTarget.Hashtag
import sh.christian.ozone.model.LinkTarget.UserDidMention
import sh.christian.ozone.model.LinkTarget.UserHandleMention
import sh.christian.ozone.model.TimelinePostLink
import sh.christian.ozone.user.MyProfileRepository
import sh.christian.ozone.user.UserDatabase
import sh.christian.ozone.user.UserHandle
import sh.christian.ozone.util.serialize
import software.ralf.app.platform.presenter.compose.ComposePresenter

@Inject
class ComposePostPresenter(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val userDatabase: UserDatabase,
  private val myProfileRepository: MyProfileRepository,
  private val errorPresenter: ErrorPresenter,
) : ComposePresenter<ComposePostPresenter.Input, OzoneTemplate> {
  @Composable
  override fun present(input: Input): OzoneTemplate {
    val profile by myProfileRepository.me().collectAsState()
    val scope = rememberCoroutineScope()
    var posting by remember(input.props) { mutableStateOf(false) }
    var error by remember(input.props) { mutableStateOf<ErrorProps?>(null) }
    val myProfile = profile ?: return OzoneTemplate.FullScreen(LoadingScreen("Loading composer…"))
    val screen = ComposePostScreen(
      profile = myProfile,
      replyingTo = input.props.replyTo?.parentAuthor,
      onExit = input.onExit,
      onPost = { payload ->
        posting = true
        scope.launch {
          when (val response = post(payload, input.props.replyTo)) {
            is AtpResponse.Success -> input.onCreated()
            is AtpResponse.Failure -> {
              posting = false
              error = response.toErrorProps(true)
                ?: ErrorProps("Oops.", "Something bad happened.", false)
            }
          }
        }
      },
    )
    val errorProps = error
    return when {
      errorProps != null -> OzoneTemplate.Modal(
        content = screen,
        modal = errorPresenter.present(ErrorPresenter.Input(errorProps) { output ->
          when (output) {
            ErrorOutput.Dismiss -> error = null
            ErrorOutput.Retry -> error = null
          }
        }),
      )
      posting -> OzoneTemplate.FullScreen(LoadingScreen("Posting…"))
      else -> OzoneTemplate.FullScreen(screen)
    }
  }

  private suspend fun post(
    post: PostPayload,
    originalPost: PostReplyInfo?,
  ) = coroutineScope {
    val resolvedLinks: List<TimelinePostLink> = post.links.map { link ->
      async {
        when (link.target) {
          is ExternalLink, is UserDidMention, is Hashtag -> link
          is UserHandleMention -> userDatabase.profileOrNull(UserHandle(link.target.handle))
            .first()
            ?.let { profile -> link.copy(target = UserDidMention(profile.did)) }
        }
      }
    }.awaitAll().filterNotNull()
    val reply = originalPost?.let { original ->
      PostReplyRef(
        root = StrongRef(original.root.uri, original.root.cid),
        parent = StrongRef(original.parent.uri, original.parent.cid),
      )
    }
    apiProvider.api.createRecord(
      CreateRecordRequest(
        repo = post.authorDid,
        collection = Nsid("app.bsky.feed.post"),
        record = Post.serializer().serialize(
          Post(
            text = post.text,
            reply = reply,
            facets = resolvedLinks.map { link ->
              Facet(
                index = FacetByteSlice(link.start.toLong(), link.end.toLong()),
                features = when (val target = link.target) {
                  is ExternalLink -> listOf(Link(FacetLink(target.uri)))
                  is UserDidMention -> listOf(Mention(FacetMention(target.did)))
                  is Hashtag -> listOf(Tag(FacetTag(target.tag)))
                  is UserHandleMention -> emptyList()
                },
              )
            },
            createdAt = clock.now(),
          )
        ),
      )
    )
  }

  data class Input(
    val props: ComposePostProps,
    val onExit: () -> Unit,
    val onCreated: () -> Unit,
  )
}
