package sh.christian.ozone.timeline

import app.bsky.actor.ProfileView
import app.bsky.feed.GetTimelineResponse
import sh.christian.ozone.compose.ComposePostProps
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.profile.ProfileProps
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.util.RemoteData

sealed interface TimelineState {
  val profile: RemoteData<ProfileView>
  val timeline: RemoteData<GetTimelineResponse>

  data class FetchingTimeline(
    override val profile: RemoteData<ProfileView>,
    override val timeline: RemoteData<GetTimelineResponse>,
  ) : TimelineState

  data class ShowingTimeline(
    override val profile: RemoteData.Success<ProfileView>,
    override val timeline: RemoteData.Success<GetTimelineResponse>,
  ) : TimelineState

  data class ShowingProfile(
    override val profile: RemoteData<ProfileView>,
    override val timeline: RemoteData.Success<GetTimelineResponse>,
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
    override val profile: RemoteData<ProfileView>,
    override val timeline: RemoteData<GetTimelineResponse>,
  ) : TimelineState {
    val error: ErrorProps
      get() = (profile as? RemoteData.Failed)?.error
        ?: (timeline as? RemoteData.Failed)?.error
        ?: error("No error found: profile=$profile, timeline=$timeline")
  }
}
