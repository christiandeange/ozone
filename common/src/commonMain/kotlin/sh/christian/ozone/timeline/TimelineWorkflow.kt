package sh.christian.ozone.timeline

import app.bsky.actor.ProfileView
import app.bsky.feed.GetTimelineQueryParams
import app.bsky.feed.GetTimelineResponse
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import kotlinx.datetime.Clock
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.compose.ComposePostOutput
import sh.christian.ozone.compose.ComposePostProps
import sh.christian.ozone.compose.ComposePostWorkflow
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.timeline.TimelineOutput.CloseApp
import sh.christian.ozone.timeline.TimelineOutput.SignOut
import sh.christian.ozone.timeline.TimelineState.ComposingPost
import sh.christian.ozone.timeline.TimelineState.FetchingTimeline
import sh.christian.ozone.timeline.TimelineState.ShowingError
import sh.christian.ozone.timeline.TimelineState.ShowingFullSizeImage
import sh.christian.ozone.timeline.TimelineState.ShowingTimeline
import sh.christian.ozone.ui.compose.ImageOverlayScreen
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.Dismissable.DismissHandler

class TimelineWorkflow(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val profileRepository: ProfileRepository,
  private val composePostWorkflow: ComposePostWorkflow,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<TimelineProps, TimelineState, TimelineOutput, AppScreen>() {

  override fun initialState(
    props: TimelineProps,
    snapshot: Snapshot?,
  ): TimelineState = FetchingTimeline(profile = null, timeline = null)

  override fun render(
    renderProps: TimelineProps,
    renderState: TimelineState,
    context: RenderContext
  ): AppScreen {
    context.runningWorker(profileRepository.profile().asWorker()) { newProfile ->
      action {
        state = state.withProfile(newProfile)
      }
    }

    return when (renderState) {
      is FetchingTimeline -> {
        context.runningWorker(loadTimeline(cursor = renderState.timeline?.cursor)) { result ->
          action {
            val existingTimeline = state.timeline
            val loadedTimeline = result.maybeResponse()

            val mergedTimeline = if (existingTimeline != null && loadedTimeline != null) {
              GetTimelineResponse(
                cursor = loadedTimeline.cursor,
                feed = existingTimeline.feed + loadedTimeline.feed,
              )
            } else {
              existingTimeline ?: loadedTimeline
            }

            state = if (mergedTimeline != null) {
              ShowingTimeline(state.profile, mergedTimeline)
            } else {
              val errorProps = ErrorProps.CustomError("Oops.", "Could not load profile.", true)
              ShowingError(state.profile, null, errorProps)
            }
          }
        }

        val overlay = if (renderState.timeline == null) {
          TextOverlayScreen(
            onDismiss = Dismissable.Ignore,
            text = "Loading timeline for ${renderProps.authInfo.handle}...",
          )
        } else {
          null
        }

        AppScreen(
          main = context.timelineScreen(renderState.profile, renderState.timeline),
          overlay = overlay,
        )
      }
      is ShowingTimeline -> {
        AppScreen(context.timelineScreen(renderState.profile, renderState.timeline))
      }
      is ShowingFullSizeImage -> {
        AppScreen(
          context.timelineScreen(renderState.profile, renderState.timeline),
          ImageOverlayScreen(
            onDismiss = DismissHandler(
              context.eventHandler { state = renderState.previousState }
            ),
            action = renderState.openImageAction,
          )
        )
      }
      is ComposingPost -> {
        AppScreen(
          context.renderChild(composePostWorkflow, renderState.props) { output ->
            action {
              val profile: ProfileView? = state.profile
              val timeline: GetTimelineResponse? = state.timeline

              state = when (output) {
                is ComposePostOutput.CreatedPost -> {
                  FetchingTimeline(profile, timeline)
                }
                is ComposePostOutput.CanceledPost -> {
                  if (profile == null || timeline == null) {
                    FetchingTimeline(profile, timeline)
                  } else {
                    ShowingTimeline(profile, timeline)
                  }
                }
              }
            }
          })
      }
      is ShowingError -> {
        AppScreen(
          main = context.timelineScreen(renderState.profile, renderState.timeline),
          overlay = context.renderChild(errorWorkflow, renderState.errorProps) { output ->
            action {
              val oldProfile = state.profile
              val oldTimeline = state.timeline
              when (output) {
                ErrorOutput.Dismiss -> {
                  if (oldTimeline == null && oldProfile == null) {
                    setOutput(SignOut)
                  } else if (oldTimeline == null || oldProfile == null) {
                    state = FetchingTimeline(oldProfile, oldTimeline)
                  } else {
                    state = ShowingTimeline(oldProfile, oldTimeline)
                  }
                }
                ErrorOutput.Retry -> state = FetchingTimeline(oldProfile, oldTimeline)
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: TimelineState): Snapshot? = null

  private fun RenderContext.timelineScreen(
    profile: ProfileView?,
    timelineResponse: GetTimelineResponse?,
  ): TimelineScreen {
    return TimelineScreen(
      now = clock.now(),
      profile = profile,
      timeline = timelineResponse?.feed.orEmpty(),
      showComposePostButton = profile != null && timelineResponse != null,
      onLoadMore = eventHandler {
        state = FetchingTimeline(state.profile, state.timeline)
      },
      onComposePost = eventHandler {
        state = ComposingPost(timelineResponse!!, ComposePostProps(profile!!))
      },
      onOpenImage = eventHandler { action ->
        state = ShowingFullSizeImage(state, action)
      },
      onSignOut = eventHandler {
        setOutput(SignOut)
      },
      onExit = eventHandler {
        setOutput(CloseApp)
      },
    )
  }

  private fun loadTimeline(cursor: String?): Worker<AtpResponse<GetTimelineResponse>> {
    return Worker.from {
      apiProvider.api.getTimeline(
        GetTimelineQueryParams(
          limit = 100,
          before = cursor,
        )
      )
    }
  }

  private fun TimelineState.withProfile(profile: ProfileView?): TimelineState = when (this) {
    is ComposingPost -> profile?.let { copy(props = props.copy(profile = it)) } ?: this
    is FetchingTimeline -> copy(profile = profile)
    is ShowingError -> copy(profile = profile)
    is ShowingFullSizeImage -> copy(previousState = previousState.withProfile(profile))
    is ShowingTimeline -> copy(profile = profile)
  }
}
