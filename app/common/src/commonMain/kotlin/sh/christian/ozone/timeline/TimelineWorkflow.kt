package sh.christian.ozone.timeline

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.compose.ComposePostProps
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.home.HomeSubDestination
import sh.christian.ozone.model.FullProfile
import sh.christian.ozone.model.Moment
import sh.christian.ozone.model.Timeline
import sh.christian.ozone.profile.ProfileProps
import sh.christian.ozone.timeline.TimelineOutput.CloseApp
import sh.christian.ozone.timeline.TimelineOutput.EnterScreen
import sh.christian.ozone.timeline.TimelineState.FetchingTimeline
import sh.christian.ozone.timeline.TimelineState.ShowingError
import sh.christian.ozone.timeline.TimelineState.ShowingFullSizeImage
import sh.christian.ozone.timeline.TimelineState.ShowingTimeline
import sh.christian.ozone.ui.compose.ImageOverlayScreen
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.Dismissable.DismissHandler
import sh.christian.ozone.user.MyProfileRepository
import kotlin.time.Duration.Companion.minutes

@Inject
class TimelineWorkflow(
  private val clock: Clock,
  private val myProfileRepository: MyProfileRepository,
  private val timelineRepository: TimelineRepository,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<TimelineProps, TimelineState, TimelineOutput, AppScreen>() {

  override fun initialState(
    props: TimelineProps,
    snapshot: Snapshot?,
  ): TimelineState = FetchingTimeline(
    profile = myProfileRepository.me().value,
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
            ShowingTimeline(existingProfile, newTimeline, showRefreshPrompt = false)
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

    val timelineScreen = context.timelineScreen(
      profile = renderState.profile,
      timelineResponse = renderState.timeline,
      showRefreshPrompt = (renderState as? ShowingTimeline)?.showRefreshPrompt == true,
    )

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

        val overlay = TextOverlayScreen(
          onDismiss = Dismissable.Ignore,
          text = "Loading timeline for ${renderProps.authInfo.handle}...",
        ).takeIf { renderState.timeline == null }

        AppScreen(
          main = timelineScreen,
          overlay = overlay,
        )
      }
      is ShowingTimeline -> {
        context.runningWorker(Worker.timer(1.minutes.inWholeMilliseconds)) {
          action {
            val currentState = state
            if (currentState is ShowingTimeline) {
              state = currentState.copy(showRefreshPrompt = true)
            }
          }
        }

        AppScreen(main = timelineScreen)
      }
      is ShowingFullSizeImage -> {
        AppScreen(
          main = timelineScreen,
          overlay = ImageOverlayScreen(
            onDismiss = DismissHandler(
              context.eventHandler { state = renderState.previousState }
            ),
            action = renderState.openImageAction,
          )
        )
      }
      is ShowingError -> {
        AppScreen(
          main = timelineScreen,
          overlay = context.renderChild(errorWorkflow, renderState.props) { output ->
            action {
              when (output) {
                ErrorOutput.Dismiss -> setOutput(CloseApp)
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
    showRefreshPrompt: Boolean,
  ): TimelineScreen {
    return TimelineScreen(
      now = Moment(clock.now()),
      profile = profile,
      timeline = timelineResponse?.posts.orEmpty().toImmutableList(),
      showRefreshPrompt = showRefreshPrompt,
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
        val props = ComposePostProps(replyTo = null)
        setOutput(EnterScreen(HomeSubDestination.GoToComposePost(props)))
      },
      onOpenPost = eventHandler { props ->
        setOutput(EnterScreen(HomeSubDestination.GoToThread(props)))
      },
      onOpenUser = eventHandler { user ->
        val props = ProfileProps(user, profile?.takeIf { myProfileRepository.isMe(user) })
        setOutput(EnterScreen(HomeSubDestination.GoToProfile(props)))
      },
      onOpenImage = eventHandler { action ->
        state = ShowingFullSizeImage(state, action)
      },
      onReplyToPost = eventHandler { postInfo ->
        val props = ComposePostProps(replyTo = postInfo)
        setOutput(EnterScreen(HomeSubDestination.GoToComposePost(props)))
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
          ShowingTimeline(profile, timeline!!, showRefreshPrompt = false)
        } else {
          copy(profile = profile)
        }
      }
      is ShowingTimeline -> {
        copy(profile = profile)
      }
      is ShowingFullSizeImage -> {
        copy(previousState = previousState.withProfile(profile))
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
      is ShowingFullSizeImage -> copy(previousState = previousState.withTimeline(timeline))
      is ShowingError -> copy(previousState = previousState.withTimeline(timeline))
    }
  }
}
