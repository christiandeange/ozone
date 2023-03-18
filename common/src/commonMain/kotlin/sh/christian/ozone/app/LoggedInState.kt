package sh.christian.ozone.app

import app.bsky.feed.GetTimelineResponse
import sh.christian.ozone.error.ErrorProps

sealed interface LoggedInState {
  val timeline: GetTimelineResponse?

  data class FetchingTimeline(
    override val timeline: GetTimelineResponse?,
  ) : LoggedInState

  data class ShowingTimeline(
    override val timeline: GetTimelineResponse,
  ) : LoggedInState

  data class ShowingError(
    override val timeline: GetTimelineResponse?,
    val errorProps: ErrorProps,
  ) : LoggedInState
}
