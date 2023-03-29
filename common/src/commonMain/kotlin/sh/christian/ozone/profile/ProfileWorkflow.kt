package sh.christian.ozone.profile

import app.bsky.actor.GetProfileQueryParams
import app.bsky.actor.ProfileView
import app.bsky.feed.FeedViewPost
import app.bsky.feed.GetAuthorFeedQueryParams
import app.bsky.feed.GetAuthorFeedResponse
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import kotlinx.datetime.Clock
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.NetworkWorker
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.profile.ProfileState.FetchingProfile
import sh.christian.ozone.profile.ProfileState.ShowingError
import sh.christian.ozone.profile.ProfileState.ShowingFullSizeImage
import sh.christian.ozone.profile.ProfileState.ShowingProfile
import sh.christian.ozone.ui.compose.ImageOverlayScreen
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.EmptyScreen
import sh.christian.ozone.user.UserDatabase
import sh.christian.ozone.user.UserReference
import sh.christian.ozone.util.RemoteData
import sh.christian.ozone.util.RemoteData.Failed
import sh.christian.ozone.util.RemoteData.Fetching
import sh.christian.ozone.util.RemoteData.Success

class ProfileWorkflow(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val userDatabase: UserDatabase,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<ProfileProps, ProfileState, Unit, AppScreen>() {
  override fun initialState(
    props: ProfileProps,
    snapshot: Snapshot?,
  ): ProfileState = FetchingProfile(
    profile = Fetching(props.preloadedProfile),
    feed = Fetching(),
  )

  override fun render(
    renderProps: ProfileProps,
    renderState: ProfileState,
    context: RenderContext,
  ): AppScreen {
    context.runningWorker(userDatabase.profile(renderProps.user).asWorker()) { result ->
      action {
        state = determineState(Success(result), state.feed)
      }
    }

    if (renderState.feed is Fetching) {
      val worker = loadPosts(renderProps.user, renderState.feed.getOrNull()?.cursor)
      context.runningWorker(worker) { result ->
        action {
          val feedResult = RemoteData.fromAtpResponseOrError(result, state.feed) {
            ErrorProps.CustomError("Oops.", "Could not load feed for @${props.user}.", true)
          }
          val combinedFeed = if (feedResult is Success) {
            val oldFeed = state.feed.getOrNull()?.feed.orEmpty()
            val newFeed = feedResult.getOrNull()?.feed.orEmpty()
            Success(
              GetAuthorFeedResponse(
                cursor = feedResult.value.cursor,
                feed = oldFeed + newFeed,
              )
            )
          } else {
            feedResult
          }

          state = determineState(state.profile, combinedFeed)
        }
      }
    }

    return when (renderState) {
      is FetchingProfile -> {
        val profileView = renderState.profile.getOrNull()
        val feed = renderState.feed.getOrNull()
        if (profileView != null) {
          AppScreen(context.profileScreen(renderProps, profileView, feed?.feed.orEmpty()))
        } else {
          AppScreen(
            EmptyScreen,
            TextOverlayScreen(
              onDismiss = Dismissable.Ignore,
              text = "Loading @${renderProps.user}...",
            ),
          )
        }
      }
      is ShowingProfile -> {
        AppScreen(
          context.profileScreen(
            props = renderProps,
            profileView = renderState.profile.value,
            feed = renderState.feed.value.feed,
          )
        )
      }
      is ShowingFullSizeImage -> {
        AppScreen(
          context.profileScreen(
            props = renderProps,
            profileView = renderState.previousState.profile.value,
            feed = renderState.previousState.feed.value.feed,
          ),
          ImageOverlayScreen(
            onDismiss = Dismissable.DismissHandler(
              context.eventHandler { state = renderState.previousState }
            ),
            action = renderState.openImageAction,
          ),
        )
      }
      is ShowingError -> {
        val mainScreen = renderState.profile.getOrNull()
          ?.let {
            context.profileScreen(
              props = renderProps,
              profileView = it,
              feed = renderState.feed.getOrNull()?.feed.orEmpty(),
            )
          }
          ?: EmptyScreen

        AppScreen(
          mainScreen,
          context.renderChild(errorWorkflow, renderState.error) { output ->
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
                  state = FetchingProfile(newProfile, newFeed)
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
    props: ProfileProps,
    profileView: ProfileView,
    feed: List<FeedViewPost>,
  ): ProfileScreen {
    return ProfileScreen(
      now = clock.now(),
      profileView = profileView,
      feed = feed,
      isSelf = props.isSelf,
      onLoadMore = eventHandler {
        state = FetchingProfile(
          profile = state.profile,
          feed = Fetching(state.feed.getOrNull()),
        )
      },
      onOpenUser = eventHandler { user ->
        // TODO
      },
      onOpenImage = eventHandler { action ->
        state = ShowingFullSizeImage(state as ShowingProfile, action)
      },
      onExit = eventHandler {
        setOutput(Unit)
      },
    )
  }

  private fun determineState(
    profile: RemoteData<ProfileView>,
    feed: RemoteData<GetAuthorFeedResponse>,
  ): ProfileState {
    return if (profile is Success && feed is Success) {
      ShowingProfile(profile, feed)
    } else if (profile is Fetching || feed is Fetching) {
      FetchingProfile(profile, feed)
    } else if (profile is Failed || feed is Failed) {
      ShowingError(profile, feed)
    } else {
      error("Unknown state to transition to with profile=$profile, feed=$feed")
    }
  }

  private fun loadProfile(user: UserReference): Worker<AtpResponse<ProfileView>> = NetworkWorker {
    val identifier = when (user) {
      is UserReference.Did -> user.did
      is UserReference.Handle -> user.handle
    }
    apiProvider.api.getProfile(GetProfileQueryParams(identifier))
  }

  private fun loadPosts(
    user: UserReference,
    cursor: String?,
  ): Worker<AtpResponse<GetAuthorFeedResponse>> = NetworkWorker {
    val identifier = when (user) {
      is UserReference.Did -> user.did
      is UserReference.Handle -> user.handle
    }
    apiProvider.api.getAuthorFeed(
      GetAuthorFeedQueryParams(
        author = identifier,
        limit = 100,
        before = cursor,
      )
    )
  }
}
