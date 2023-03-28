package sh.christian.ozone.profile

import app.bsky.actor.ProfileView
import sh.christian.ozone.error.ErrorProps

sealed interface ProfileState {
  val profileView: ProfileView?

  data class FetchingProfile(
    override val profileView: ProfileView?,
  ) : ProfileState

  data class ShowingProfile(
    override val profileView: ProfileView,
  ) : ProfileState

  data class ShowingError(
    val props: ErrorProps,
    override val profileView: ProfileView?,
  ) : ProfileState
}
