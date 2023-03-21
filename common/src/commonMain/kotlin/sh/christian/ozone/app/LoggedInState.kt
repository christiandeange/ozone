package sh.christian.ozone.app

import app.bsky.actor.ProfileView
import app.bsky.feed.GetTimelineResponse
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.ui.compose.OpenImageAction

sealed interface LoggedInState {
  val profile: ProfileView?
  val timeline: GetTimelineResponse?

  data class FetchingTimeline(
    override val profile: ProfileView?,
    override val timeline: GetTimelineResponse?,
  ) : LoggedInState

  data class ShowingTimeline(
    override val profile: ProfileView,
    override val timeline: GetTimelineResponse,
  ) : LoggedInState

  data class ShowingFullSizeImage(
    val previousState: LoggedInState,
    val openImageAction: OpenImageAction,
  ) : LoggedInState by previousState

  data class ShowingError(
    override val profile: ProfileView?,
    override val timeline: GetTimelineResponse?,
    val errorProps: ErrorProps,
  ) : LoggedInState
}
