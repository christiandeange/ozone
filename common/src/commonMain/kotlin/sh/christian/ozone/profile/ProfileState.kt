package sh.christian.ozone.profile

import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.model.Profile
import sh.christian.ozone.model.Timeline
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.util.RemoteData
import sh.christian.ozone.util.RemoteData.Failed
import sh.christian.ozone.util.RemoteData.Success

sealed interface ProfileState {
  val profile: RemoteData<Profile>
  val feed: RemoteData<Timeline>

  data class FetchingProfile(
    override val profile: RemoteData<Profile>,
    override val feed: RemoteData<Timeline>,
  ) : ProfileState

  data class ShowingProfile(
    override val profile: Success<Profile>,
    override val feed: Success<Timeline>,
  ) : ProfileState

  data class ShowingFullSizeImage(
    val previousState: ShowingProfile,
    val openImageAction: OpenImageAction,
  ) : ProfileState by previousState

  data class ShowingError(
    override val profile: RemoteData<Profile>,
    override val feed: RemoteData<Timeline>,
  ) : ProfileState {
    val error: ErrorProps
      get() = (profile as? Failed)?.error
        ?: (feed as? Failed)?.error
        ?: error("No error found: profile=$profile, feed=$feed")
  }
}
