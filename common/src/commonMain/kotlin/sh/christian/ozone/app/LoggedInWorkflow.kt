package sh.christian.ozone.app

import app.bsky.feed.GetTimelineQueryParams
import app.bsky.feed.GetTimelineResponse
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.LoggedInOutput.CloseApp
import sh.christian.ozone.app.LoggedInOutput.SignOut
import sh.christian.ozone.app.LoggedInState.FetchingTimeline
import sh.christian.ozone.app.LoggedInState.ShowingError
import sh.christian.ozone.app.LoggedInState.ShowingTimeline
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.home.TimelineScreen
import sh.christian.ozone.ui.compose.Dismissable
import sh.christian.ozone.ui.compose.OverlayScreen
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.plus

class LoggedInWorkflow(
  private val apiProvider: ApiProvider,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<LoggedInProps, LoggedInState, LoggedInOutput, ViewRendering>() {

  override fun initialState(
    props: LoggedInProps,
    snapshot: Snapshot?,
  ): LoggedInState = FetchingTimeline(timeline = null)

  override fun render(
    renderProps: LoggedInProps,
    renderState: LoggedInState,
    context: RenderContext
  ): ViewRendering = when (renderState) {
    is FetchingTimeline -> {
      context.runningWorker(loadTimeline()) { result ->
        action {
          state = when (result) {
            is AtpResponse.Success -> {
              ShowingTimeline(result.response)
            }
            is AtpResponse.Failure -> {
              val errorProps = result.toErrorProps(true)
                ?: ErrorProps.CustomError("Oops.", "Something bad happened", false)
              ShowingError(state.timeline, errorProps)
            }
          }
        }
      }

      context.timelineScreen(renderState.timeline) + OverlayScreen(
        text = "Loading timeline for ${renderProps.authInfo.handle}...",
        onDismiss = Dismissable.Ignore,
      )
    }
    is ShowingTimeline -> context.timelineScreen(renderState.timeline)
    is ShowingError -> {
      context.timelineScreen(renderState.timeline) + context.renderChild(
        errorWorkflow, renderState.errorProps
      ) { output ->
        action {
          when (output) {
            ErrorOutput.Dismiss -> {
              val oldTimeline = state.timeline
              if (oldTimeline == null) {
                setOutput(CloseApp)
              } else {
                state = ShowingTimeline(oldTimeline)
              }
            }
            ErrorOutput.Retry -> state = FetchingTimeline(null)
          }
        }
      }
    }
  }

  override fun snapshotState(state: LoggedInState): Snapshot? = null

  private fun RenderContext.timelineScreen(
    timelineResponse: GetTimelineResponse?,
  ): TimelineScreen {
    return TimelineScreen(
      timeline = timelineResponse.toString(),
      onSignOut = eventHandler {
        setOutput(SignOut)
      },
      onExit = eventHandler {
        setOutput(CloseApp)
      },
    )
  }

  private fun loadTimeline(): Worker<AtpResponse<GetTimelineResponse>> {
    return Worker.from { apiProvider.api.getTimeline(GetTimelineQueryParams("", 10L, "")) }
  }
}
