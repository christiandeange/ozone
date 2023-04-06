package sh.christian.ozone.timeline

import app.bsky.feed.GetTimelineQueryParams
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
import sh.christian.ozone.compose.ComposePostOutput
import sh.christian.ozone.compose.ComposePostProps
import sh.christian.ozone.compose.ComposePostWorkflow
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.model.FullProfile
import sh.christian.ozone.model.Timeline
import sh.christian.ozone.profile.ProfileProps
import sh.christian.ozone.profile.ProfileWorkflow
import sh.christian.ozone.timeline.TimelineOutput.CloseApp
import sh.christian.ozone.timeline.TimelineOutput.SignOut
import sh.christian.ozone.timeline.TimelineState.ComposingPost
import sh.christian.ozone.timeline.TimelineState.FetchingTimeline
import sh.christian.ozone.timeline.TimelineState.ShowingError
import sh.christian.ozone.timeline.TimelineState.ShowingFullSizeImage
import sh.christian.ozone.timeline.TimelineState.ShowingProfile
import sh.christian.ozone.timeline.TimelineState.ShowingTimeline
import sh.christian.ozone.ui.compose.ImageOverlayScreen
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.Dismissable.DismissHandler
import sh.christian.ozone.ui.workflow.EmptyScreen
import sh.christian.ozone.ui.workflow.plus
import sh.christian.ozone.user.MyProfileRepository
import sh.christian.ozone.user.UserReference
import sh.christian.ozone.util.RemoteData

class TimelineWorkflow(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val myProfileRepository: MyProfileRepository,
  private val composePostWorkflow: ComposePostWorkflow,
  private val profileWorkflow: ProfileWorkflow,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<TimelineProps, TimelineState, TimelineOutput, AppScreen>() {

  override fun initialState(
    props: TimelineProps,
    snapshot: Snapshot?,
  ): TimelineState = FetchingTimeline(
    profile = RemoteData.Fetching(),
    timeline = RemoteData.Fetching(),
  )

  override fun render(
    renderProps: TimelineProps,
    renderState: TimelineState,
    context: RenderContext
  ): AppScreen {
    val myProfileWorker = myProfileRepository.me().asWorker()
    context.runningWorker(myProfileWorker) { profile ->
      action {
        state = state.withProfile(profile)
      }
    }

    if (renderState.timeline is RemoteData.Fetching) {
      val cursor = (renderState.timeline as? RemoteData.Fetching)?.previous?.cursor
      context.runningWorker(loadTimeline(cursor = cursor)) { result ->
        action {
          val feedResult = RemoteData.fromAtpResponseOrError(result, state.timeline) {
            ErrorProps.CustomError("Oops.", "Could not load timeline", true)
          }
          val combinedTimeline = if (feedResult is RemoteData.Success) {
            val oldPosts = state.timeline.getOrNull()?.posts.orEmpty()
            val newPosts = feedResult.getOrNull()?.posts.orEmpty()
            RemoteData.Success(
              Timeline(
                cursor = feedResult.value.cursor,
                posts = oldPosts + newPosts,
              )
            )
          } else {
            feedResult
          }

          state = determineState(state.profile, combinedTimeline)
        }
      }
    }

    return when (renderState) {
      is FetchingTimeline -> {
        val profileView = renderState.profile.getOrNull()
        val timeline = renderState.timeline.getOrNull()

        val overlay = if (timeline == null) {
          TextOverlayScreen(
            onDismiss = Dismissable.Ignore,
            text = "Loading timeline for ${renderProps.authInfo.handle}...",
          )
        } else {
          null
        }

        AppScreen(
          main = context.timelineScreen(profileView, timeline),
          overlay = overlay,
        )
      }
      is ShowingTimeline -> {
        AppScreen(main = context.timelineScreen(renderState.profile.value, renderState.timeline.value))
      }
      is ShowingFullSizeImage -> {
        AppScreen(
          main = context.timelineScreen(
            profile = renderState.previousState.profile.value,
            timelineResponse = renderState.previousState.timeline.value,
          ),
          overlay = ImageOverlayScreen(
            onDismiss = DismissHandler(
              context.eventHandler { state = renderState.previousState }
            ),
            action = renderState.openImageAction,
          )
        )
      }
      is ShowingProfile -> {
        val profile = renderState.profile.getOrNull()
        val timelineResponse = renderState.timeline.value

        AppScreen(
          main = context.timelineScreen(profile, timelineResponse) +
              context.renderChild(profileWorkflow, renderState.props) {
                action {
                  state = ShowingTimeline(
                    profile = RemoteData.Success(state.profile.getOrNull()!!),
                    timeline = renderState.timeline,
                  )
                }
              }
        )
      }
      is ComposingPost -> {
        AppScreen(
          main = context.renderChild(composePostWorkflow, renderState.props) { output ->
            action {
              state = when (output) {
                is ComposePostOutput.CreatedPost -> {
                  FetchingTimeline(state.profile, state.timeline)
                }
                is ComposePostOutput.CanceledPost -> {
                  renderState.previousState
                }
              }
            }
          }
        )
      }
      is ShowingError -> {
        val mainScreen = renderState.profile.getOrNull()
          ?.let {
            context.timelineScreen(
              profile = it,
              timelineResponse = renderState.timeline.getOrNull(),
            )
          }
          ?: EmptyScreen

        AppScreen(
          main = mainScreen,
          overlay = context.renderChild(errorWorkflow, renderState.error) { output ->
            action {
              val currentProfile = state.profile
              val currentTimeline = state.timeline
              when (output) {
                ErrorOutput.Dismiss -> {
                  setOutput(SignOut)
                }
                ErrorOutput.Retry -> {
                  val newProfile = when (currentProfile) {
                    is RemoteData.Fetching -> currentProfile
                    is RemoteData.Success -> currentProfile
                    is RemoteData.Failed -> RemoteData.Fetching(currentProfile.previous)
                  }
                  val newFeed = when (currentTimeline) {
                    is RemoteData.Fetching -> currentTimeline
                    is RemoteData.Success -> currentTimeline
                    is RemoteData.Failed -> RemoteData.Fetching(currentTimeline.previous)
                  }
                  state = FetchingTimeline(newProfile, newFeed)
                }
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: TimelineState): Snapshot? = null

  private fun determineState(
    profile: RemoteData<FullProfile>,
    timeline: RemoteData<Timeline>,
  ): TimelineState {
    return if (profile is RemoteData.Success && timeline is RemoteData.Success) {
      ShowingTimeline(profile, timeline)
    } else if (profile is RemoteData.Fetching || timeline is RemoteData.Fetching) {
      FetchingTimeline(profile, timeline)
    } else if (profile is RemoteData.Failed || timeline is RemoteData.Failed) {
      ShowingError(profile, timeline)
    } else {
      error("Unknown state to transition to with profile=$profile, timeline=$timeline")
    }
  }

  private fun RenderContext.timelineScreen(
    profile: FullProfile?,
    timelineResponse: Timeline?,
  ): TimelineScreen {
    return TimelineScreen(
      now = clock.now(),
      profile = profile,
      timeline = timelineResponse?.posts.orEmpty(),
      showComposePostButton = profile != null && timelineResponse != null,
      onLoadMore = eventHandler {
        state = FetchingTimeline(
          profile = state.profile,
          timeline = RemoteData.Fetching(state.timeline.getOrNull()),
        )
      },
      onComposePost = eventHandler {
        state = ComposingPost(state, ComposePostProps(profile!!))
      },
      onOpenUser = eventHandler { user ->
        val isMe = when (user) {
          is UserReference.Did -> user.did == state.profile.getOrNull()?.did
          is UserReference.Handle -> user.handle == state.profile.getOrNull()?.handle
        }
        state = ShowingProfile(
          profile = state.profile,
          timeline = RemoteData.Success(state.timeline.getOrNull()!!),
          props = ProfileProps(user, isMe, profile?.takeIf { isMe }),
        )
      },
      onOpenImage = eventHandler { action ->
        state = ShowingFullSizeImage(state as ShowingTimeline, action)
      },
      onSignOut = eventHandler {
        setOutput(SignOut)
      },
      onExit = eventHandler {
        setOutput(CloseApp)
      },
    )
  }

  private fun loadTimeline(cursor: String?): Worker<AtpResponse<Timeline>> {
    return NetworkWorker {
      apiProvider.api.getTimeline(
        GetTimelineQueryParams(
          limit = 100,
          cursor = cursor,
        )
      ).map { Timeline.from(it.feed, it.cursor) }
    }
  }

  private fun TimelineState.withProfile(profileResult: FullProfile?): TimelineState {
    val profile = if (profileResult == null) {
      RemoteData.Failed(ErrorProps.CustomError("Oops.", "Could not load your profile.", true))
    } else {
      RemoteData.Success(profileResult)
    }

    return when (this) {
      is ComposingPost -> {
        copy(
          previousState = previousState.withProfile(profileResult) as ShowingTimeline,
          props = profileResult?.let { props.copy(profile = it) } ?: props,
        )
      }
      is FetchingTimeline -> {
        copy(profile = profile)
      }
      is ShowingError -> {
        copy(profile = profile)
      }
      is ShowingFullSizeImage -> {
        copy(previousState = previousState.withProfile(profileResult) as ShowingTimeline)
      }
      is ShowingTimeline -> {
        copy(profile = profile as RemoteData.Success<FullProfile>)
      }
      is ShowingProfile -> {
        copy(profile = profile)
      }
    }
  }
}
