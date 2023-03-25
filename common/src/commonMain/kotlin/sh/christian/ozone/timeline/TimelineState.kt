package sh.christian.ozone.timeline

import app.bsky.actor.ProfileView
import app.bsky.feed.GetTimelineResponse
import sh.christian.ozone.compose.ComposePostProps
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.ui.compose.OpenImageAction

sealed interface TimelineState {
  val profile: ProfileView?
  val timeline: GetTimelineResponse?

  data class FetchingTimeline(
    override val profile: ProfileView?,
    override val timeline: GetTimelineResponse?,
  ) : TimelineState

  data class ShowingTimeline(
    override val profile: ProfileView,
    override val timeline: GetTimelineResponse,
  ) : TimelineState

  data class ShowingFullSizeImage(
    val previousState: TimelineState,
    val openImageAction: OpenImageAction,
  ) : TimelineState by previousState

  data class ComposingPost(
    override val timeline: GetTimelineResponse,
    val composePostProps: ComposePostProps,
  ) : TimelineState {
    override val profile: ProfileView get() = composePostProps.profile
  }

  data class ShowingError(
    override val profile: ProfileView?,
    override val timeline: GetTimelineResponse?,
    val errorProps: ErrorProps,
  ) : TimelineState
}