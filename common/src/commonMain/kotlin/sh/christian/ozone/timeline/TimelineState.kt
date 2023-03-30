package sh.christian.ozone.timeline

import sh.christian.ozone.compose.ComposePostProps
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.model.Profile
import sh.christian.ozone.model.Timeline
import sh.christian.ozone.profile.ProfileProps
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.util.RemoteData

sealed interface TimelineState {
  val profile: RemoteData<Profile>
  val timeline: RemoteData<Timeline>

  data class FetchingTimeline(
    override val profile: RemoteData<Profile>,
    override val timeline: RemoteData<Timeline>,
  ) : TimelineState

  data class ShowingTimeline(
    override val profile: RemoteData.Success<Profile>,
    override val timeline: RemoteData.Success<Timeline>,
  ) : TimelineState

  data class ShowingProfile(
    override val profile: RemoteData<Profile>,
    override val timeline: RemoteData.Success<Timeline>,
    val props: ProfileProps,
  ) : TimelineState

  data class ShowingFullSizeImage(
    val previousState: ShowingTimeline,
    val openImageAction: OpenImageAction,
  ) : TimelineState by previousState

  data class ComposingPost(
    val previousState: TimelineState,
    val props: ComposePostProps,
  ) : TimelineState by previousState

  data class ShowingError(
    override val profile: RemoteData<Profile>,
    override val timeline: RemoteData<Timeline>,
  ) : TimelineState {
    val error: ErrorProps
      get() = (profile as? RemoteData.Failed)?.error
        ?: (timeline as? RemoteData.Failed)?.error
        ?: error("No error found: profile=$profile, timeline=$timeline")
  }
}
