package sh.christian.ozone.timeline

import kotlinx.datetime.Instant
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.model.FullProfile
import sh.christian.ozone.model.Timeline
import sh.christian.ozone.ui.compose.OpenImageAction

sealed interface TimelineState {
  val profile: FullProfile?
  val timeline: Timeline?

  data class FetchingTimeline(
    override val profile: FullProfile?,
    override val timeline: Timeline?,
    val fullRefresh: Boolean,
  ) : TimelineState

  data class ShowingTimeline(
    override val profile: FullProfile,
    override val timeline: Timeline,
    val showRefreshPrompt: Boolean,
  ) : TimelineState

  data class ShowingFullSizeImage(
    val previousState: TimelineState,
    val openImageAction: OpenImageAction,
  ) : TimelineState by previousState

  data class ShowingError(
    val previousState: TimelineState,
    val props: ErrorProps,
  ) : TimelineState by previousState
}
