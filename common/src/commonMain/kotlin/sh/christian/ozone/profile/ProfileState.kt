package sh.christian.ozone.profile

import app.bsky.actor.ProfileView
import app.bsky.feed.GetAuthorFeedResponse
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.util.RemoteData
import sh.christian.ozone.util.RemoteData.Failed
import sh.christian.ozone.util.RemoteData.Success

sealed interface ProfileState {
  val profile: RemoteData<ProfileView>
  val feed: RemoteData<GetAuthorFeedResponse>

  data class FetchingProfile(
    override val profile: RemoteData<ProfileView>,
    override val feed: RemoteData<GetAuthorFeedResponse>,
  ) : ProfileState

  data class ShowingProfile(
    override val profile: Success<ProfileView>,
    override val feed: Success<GetAuthorFeedResponse>,
  ) : ProfileState

  data class ShowingFullSizeImage(
    val previousState: ShowingProfile,
    val openImageAction: OpenImageAction,
  ) : ProfileState by previousState

  data class ShowingError(
    override val profile: RemoteData<ProfileView>,
    override val feed: RemoteData<GetAuthorFeedResponse>,
  ) : ProfileState {
    val error: ErrorProps
      get() = (profile as? Failed)?.error
        ?: (feed as? Failed)?.error
        ?: error("No error found: profile=$profile, feed=$feed")
  }
}
