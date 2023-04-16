package sh.christian.ozone.profile

import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.model.FullProfile
import sh.christian.ozone.model.Timeline
import sh.christian.ozone.thread.ThreadProps
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.user.UserReference
import sh.christian.ozone.util.RemoteData
import sh.christian.ozone.util.RemoteData.Failed

sealed interface ProfileState {
  val user: UserReference
  val profile: RemoteData<FullProfile>
  val feed: RemoteData<Timeline>
  val previousState: ProfileState?

  data class ShowingProfile(
    override val user: UserReference,
    override val profile: RemoteData<FullProfile>,
    override val feed: RemoteData<Timeline>,
    override val previousState: ProfileState?,
  ) : ProfileState

  data class ShowingFullSizeImage(
    override val previousState: ProfileState,
    val openImageAction: OpenImageAction,
  ) : ProfileState by previousState

  data class ShowingThread(
    override val previousState: ProfileState,
    val props: ThreadProps,
  ) : ProfileState by previousState

  data class ShowingError(
    override val user: UserReference,
    override val profile: RemoteData<FullProfile>,
    override val feed: RemoteData<Timeline>,
    override val previousState: ProfileState?,
  ) : ProfileState {
    val error: ErrorProps
      get() = (profile as? Failed)?.error
        ?: (feed as? Failed)?.error
        ?: error("No error found: profile=$profile, feed=$feed")
  }
}
