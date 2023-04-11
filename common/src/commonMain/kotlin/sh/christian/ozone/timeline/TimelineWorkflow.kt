package sh.christian.ozone.timeline

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.datetime.Clock
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.compose.ComposePostOutput
import sh.christian.ozone.compose.ComposePostProps
import sh.christian.ozone.compose.ComposePostWorkflow
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.model.FullProfile
import sh.christian.ozone.model.Timeline
import sh.christian.ozone.profile.ProfileProps
import sh.christian.ozone.profile.ProfileWorkflow
import sh.christian.ozone.thread.ThreadProps
import sh.christian.ozone.thread.ThreadWorkflow
import sh.christian.ozone.timeline.TimelineOutput.CloseApp
import sh.christian.ozone.timeline.TimelineOutput.SignOut
import sh.christian.ozone.timeline.TimelineState.ComposingPost
import sh.christian.ozone.timeline.TimelineState.FetchingTimeline
import sh.christian.ozone.timeline.TimelineState.ShowingError
import sh.christian.ozone.timeline.TimelineState.ShowingFullSizeImage
import sh.christian.ozone.timeline.TimelineState.ShowingProfile
import sh.christian.ozone.timeline.TimelineState.ShowingThread
import sh.christian.ozone.timeline.TimelineState.ShowingTimeline
import sh.christian.ozone.ui.compose.ImageOverlayScreen
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.Dismissable.DismissHandler
import sh.christian.ozone.ui.workflow.plus
import sh.christian.ozone.user.MyProfileRepository

class TimelineWorkflow(
  private val clock: Clock,
  private val myProfileRepository: MyProfileRepository,
  private val timelineRepository: TimelineRepository,
  private val threadWorkflow: ThreadWorkflow,
  private val composePostWorkflow: ComposePostWorkflow,
  private val profileWorkflow: ProfileWorkflow,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<TimelineProps, TimelineState, TimelineOutput, AppScreen>() {

  override fun initialState(
    props: TimelineProps,
    snapshot: Snapshot?,
  ): TimelineState = FetchingTimeline(
    profile = null,
    timeline = null,
    fullRefresh = true,
  )

  override fun render(
    renderProps: TimelineProps,
    renderState: TimelineState,
    context: RenderContext
  ): AppScreen {
    val myProfileWorker = myProfileRepository.me().filterNotNull().asWorker()
    context.runningWorker(myProfileWorker) { profile ->
      action {
        state = state.withProfile(profile)
      }
    }

    val timelineWorker = timelineRepository.timeline.asWorker()
    context.runningWorker(timelineWorker) { newTimeline ->
      action {
        val existingProfile = state.profile
        state = if (existingProfile != null) {
          if (state is FetchingTimeline) {
            ShowingTimeline(existingProfile, newTimeline)
          } else {
            state.withTimeline(newTimeline)
          }
        } else {
          FetchingTimeline(
            profile = null,
            timeline = newTimeline,
            fullRefresh = (state as? FetchingTimeline)?.fullRefresh ?: true,
          )
        }
      }
    }

    val errorsWorker = timelineRepository.errors.asWorker()
    context.runningWorker(errorsWorker) { errorProps ->
      action {
        state = ShowingError(state, errorProps)
      }
    }

    return when (renderState) {
      is FetchingTimeline -> {
        val fullRefresh = renderState.fullRefresh
        context.runningSideEffect("fetch-timeline-$fullRefresh") {
          if (fullRefresh) {
            timelineRepository.refresh()
          } else {
            timelineRepository.loadMore()
          }
        }

        val profile = renderState.profile
        val timeline = renderState.timeline

        val overlay = TextOverlayScreen(
          onDismiss = Dismissable.Ignore,
          text = "Loading timeline for ${renderProps.authInfo.handle}...",
        ).takeIf { timeline == null }

        AppScreen(
          main = context.timelineScreen(profile, timeline),
          overlay = overlay,
        )
      }
      is ShowingTimeline -> {
        AppScreen(main = context.timelineScreen(renderState.profile, renderState.timeline))
      }
      is ShowingFullSizeImage -> {
        AppScreen(
          main = context.timelineScreen(renderState.profile, renderState.timeline),
          overlay = ImageOverlayScreen(
            onDismiss = DismissHandler(
              context.eventHandler { state = renderState.previousState }
            ),
            action = renderState.openImageAction,
          )
        )
      }
      is ShowingProfile -> {
        AppScreen(
          main = context.timelineScreen(renderState.profile, renderState.timeline) +
              context.renderChild(profileWorkflow, renderState.props) {
                action {
                  state = renderState.previousState
                }
              }
        )
      }
      is ShowingThread -> {
        AppScreen(
          main = context.timelineScreen(renderState.profile, renderState.timeline) +
              context.renderChild(threadWorkflow, renderState.props) {
                action {
                  state = renderState.previousState
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
                  FetchingTimeline(state.profile, state.timeline, fullRefresh = true)
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
        AppScreen(
          main = context.timelineScreen(
            profile = renderState.profile,
            timelineResponse = renderState.timeline,
          ),
          overlay = context.renderChild(errorWorkflow, renderState.props) { output ->
            action {
              when (output) {
                ErrorOutput.Dismiss -> setOutput(SignOut)
                ErrorOutput.Retry -> state = renderState.previousState
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: TimelineState): Snapshot? = null

  private fun RenderContext.timelineScreen(
    profile: FullProfile?,
    timelineResponse: Timeline?,
  ): TimelineScreen {
    return TimelineScreen(
      now = clock.now(),
      profile = profile,
      timeline = timelineResponse?.posts.orEmpty(),
      showComposePostButton = profile != null && timelineResponse != null,
      onRefresh = eventHandler {
        state = FetchingTimeline(
          profile = state.profile,
          timeline = state.timeline,
          fullRefresh = true,
        )
      },
      onLoadMore = eventHandler {
        state = FetchingTimeline(
          profile = state.profile,
          timeline = state.timeline,
          fullRefresh = false,
        )
      },
      onComposePost = eventHandler {
        state = ComposingPost(state, ComposePostProps(profile!!))
      },
      onOpenThread = eventHandler { post ->
        state = ShowingThread(state, ThreadProps(post))
      },
      onOpenUser = eventHandler { user ->
        state = ShowingProfile(
          previousState = state,
          props = ProfileProps(user, profile?.takeIf { myProfileRepository.isMe(user) }),
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

  private fun TimelineState.withProfile(profile: FullProfile): TimelineState {
    return when (this) {
      is FetchingTimeline -> {
        if (timeline != null) {
          ShowingTimeline(profile, timeline!!)
        } else {
          copy(profile = profile)
        }
      }
      is ShowingTimeline -> {
        copy(profile = profile)
      }
      is ShowingProfile -> {
        copy(previousState = previousState.withProfile(profile))
      }
      is ShowingThread -> {
        copy(previousState = previousState.withProfile(profile))
      }
      is ShowingFullSizeImage -> {
        copy(previousState = previousState.withProfile(profile) as ShowingTimeline)
      }
      is ComposingPost -> {
        copy(
          previousState = previousState.withProfile(profile) as ShowingTimeline,
          props = props.copy(profile = profile),
        )
      }
      is ShowingError -> {
        copy(previousState = previousState.withProfile(profile))
      }
    }
  }

  private fun TimelineState.withTimeline(timeline: Timeline): TimelineState {
    return when (this) {
      is FetchingTimeline -> copy(timeline = timeline)
      is ShowingTimeline -> copy(timeline = timeline)
      is ShowingProfile -> copy(previousState = previousState.withTimeline(timeline))
      is ShowingThread -> copy(previousState = previousState.withTimeline(timeline))
      is ShowingFullSizeImage -> copy(previousState = previousState.withTimeline(timeline))
      is ComposingPost -> copy(previousState = previousState.withTimeline(timeline))
      is ShowingError -> copy(previousState = previousState.withTimeline(timeline))
    }
  }
}
