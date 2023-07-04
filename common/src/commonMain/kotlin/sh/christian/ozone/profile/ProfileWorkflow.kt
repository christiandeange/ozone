package sh.christian.ozone.profile

import app.bsky.feed.GetAuthorFeedQueryParams
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.WorkflowAction
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.AtIdentifier
import sh.christian.ozone.api.NetworkWorker
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.compose.ComposePostOutput.CanceledPost
import sh.christian.ozone.compose.ComposePostOutput.CreatedPost
import sh.christian.ozone.compose.ComposePostProps
import sh.christian.ozone.compose.ComposePostWorkflow
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.model.FullProfile
import sh.christian.ozone.model.Moment
import sh.christian.ozone.model.Timeline
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.profile.ProfileState.ComposingReply
import sh.christian.ozone.profile.ProfileState.ShowingError
import sh.christian.ozone.profile.ProfileState.ShowingFullSizeImage
import sh.christian.ozone.profile.ProfileState.ShowingProfile
import sh.christian.ozone.profile.ProfileState.ShowingThread
import sh.christian.ozone.thread.ThreadWorkflow
import sh.christian.ozone.ui.compose.ImageOverlayScreen
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.user.MyProfileRepository
import sh.christian.ozone.user.UserDatabase
import sh.christian.ozone.user.UserReference
import sh.christian.ozone.util.RemoteData
import sh.christian.ozone.util.RemoteData.Failed
import sh.christian.ozone.util.RemoteData.Fetching
import sh.christian.ozone.util.RemoteData.Success

@Inject
class ProfileWorkflow(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val userDatabase: UserDatabase,
  private val myProfileRepository: MyProfileRepository,
  private val composePostWorkflow: ComposePostWorkflow,
  private val threadWorkflow: ThreadWorkflow,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<ProfileProps, ProfileState, Unit, AppScreen>() {
  override fun initialState(
    props: ProfileProps,
    snapshot: Snapshot?,
  ): ProfileState = ShowingProfile(
    user = props.user,
    profile = Fetching(props.preloadedProfile),
    feed = Fetching(),
    previousState = null,
  )

  override fun render(
    renderProps: ProfileProps,
    renderState: ProfileState,
    context: RenderContext,
  ): AppScreen {
    val profileWorker = userDatabase.profile(renderState.user).asWorker()
    context.runningWorker(profileWorker, renderState.user.toString()) { result ->
      action {
        state = determineState(Success(result), state.feed)
      }
    }

    if (renderState.feed is Fetching) {
      val worker = loadPosts(renderState.user, renderState.feed.getOrNull()?.cursor)
      context.runningWorker(worker, renderState.user.toString()) { result ->
        action {
          val feedResult = RemoteData.fromAtpResponseOrError(result, state.feed) {
            ErrorProps("Oops.", "Could not load feed for @${props.user}.", true)
          }
          val combinedFeed = if (feedResult is Success) {
            val oldPosts = state.feed.getOrNull()?.posts.orEmpty()
            val newPosts = feedResult.getOrNull()?.posts.orEmpty()
            Success(
              Timeline(
                cursor = feedResult.value.cursor,
                posts = (oldPosts + newPosts).toImmutableList(),
              )
            )
          } else {
            feedResult
          }

          state = determineState(state.profile, combinedFeed)
        }
      }
    }

    val screenStack = generateSequence(renderState) { it.previousState }
      .toList()
      .reversed()
      .filter { it.profile is Success }
      .map { state: ProfileState ->
        context.profileScreen(
          profile = state.profile.getOrNull()!!,
          feed = state.feed.getOrNull()?.posts.orEmpty(),
        )
      }
      .toImmutableList()

    return when (renderState) {
      is ShowingProfile -> {
        if (renderState.profile is Fetching) {
          AppScreen(
            mains = screenStack,
            overlay = TextOverlayScreen(
              onDismiss = Dismissable.Ignore,
              text = "Loading ${renderState.user}...",
            ),
          )
        } else {
          AppScreen(mains = screenStack)
        }
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
      is ComposingReply -> {
        val composeScreen = context.renderChild(composePostWorkflow, renderState.props) { output ->
          action {
            state = when (output) {
              CanceledPost -> renderState.previousState
              CreatedPost -> ShowingProfile(
                user = renderState.user,
                profile = Fetching(renderState.profile.getOrNull()),
                feed = Fetching(renderState.feed.getOrNull()),
                previousState = renderState.previousState.previousState,
              )
            }
          }
        }

        composeScreen.copy(mains = (screenStack + composeScreen.mains).toImmutableList())
      }
      is ShowingThread -> {
        val threadScreen = context.renderChild(threadWorkflow, renderState.props) {
          action {
            state = renderState.previousState
          }
        }

        threadScreen.copy(mains = (screenStack + threadScreen.mains).toImmutableList())
      }
      is ShowingError -> {
        AppScreen(
          mains = screenStack,
          overlay = context.renderChild(errorWorkflow, renderState.error) { output ->
            action {
              val currentProfile = state.profile
              val currentFeed = state.feed
              when (output) {
                ErrorOutput.Dismiss -> {
                  setOutput(Unit)
                }
                ErrorOutput.Retry -> {
                  val newProfile = when (currentProfile) {
                    is Fetching -> currentProfile
                    is Success -> currentProfile
                    is Failed -> Fetching(currentProfile.previous)
                  }
                  val newFeed = when (currentFeed) {
                    is Fetching -> currentFeed
                    is Success -> currentFeed
                    is Failed -> Fetching(currentFeed.previous)
                  }
                  state = ShowingProfile(state.user, newProfile, newFeed, state.previousState)
                }
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: ProfileState): Snapshot? = null

  private fun RenderContext.profileScreen(
    profile: FullProfile,
    feed: List<TimelinePost>,
  ): ProfileScreen {
    return ProfileScreen(
      now = Moment(clock.now()),
      profile = profile,
      feed = feed.toImmutableList(),
      isSelf = myProfileRepository.isMe(UserReference.Did(profile.did)),
      onLoadMore = eventHandler {
        state = ShowingProfile(
          user = state.user,
          profile = state.profile,
          feed = Fetching(state.feed.getOrNull()),
          previousState = state.previousState,
        )
      },
      onOpenPost = eventHandler { props ->
        state = ShowingThread(
          previousState = state,
          props = props,
        )
      },
      onOpenUser = eventHandler { user ->
        if (user != state.user) {
          state = ShowingProfile(
            user = user,
            profile = Fetching(),
            feed = Fetching(),
            previousState = state,
          )
        }
      },
      onOpenImage = eventHandler { action ->
        state = ShowingFullSizeImage(state, action)
      },
      onReplyToPost = eventHandler { postInfo ->
        state = ComposingReply(state, ComposePostProps(replyTo = postInfo))
      },
      onExit = eventHandler {
        state.previousState
          ?.let { state = it }
          ?: setOutput(Unit)
      },
    )
  }

  private fun WorkflowAction<ProfileProps, ProfileState, Unit>.Updater.determineState(
    profile: RemoteData<FullProfile>,
    feed: RemoteData<Timeline>,
  ): ProfileState {
    return if (profile !is Failed && feed !is Failed) {
      ShowingProfile(state.user, profile, feed, state.previousState)
    } else {
      ShowingError(state.user, profile, feed, state.previousState)
    }
  }

  private fun loadPosts(
    user: UserReference,
    cursor: String?,
  ): Worker<AtpResponse<Timeline>> = NetworkWorker {
    val identifier = when (user) {
      is UserReference.Did -> AtIdentifier(user.did.did)
      is UserReference.Handle -> AtIdentifier(user.handle.handle)
    }
    apiProvider.api.getAuthorFeed(
      GetAuthorFeedQueryParams(
        actor = identifier,
        limit = 100,
        cursor = cursor,
      )
    ).map { Timeline.from(it.feed, it.cursor) }
  }
}
