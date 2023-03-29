package sh.christian.ozone.profile

import app.bsky.actor.ProfileView
import sh.christian.ozone.user.UserReference

data class ProfileProps(
  val user: UserReference,
  val isSelf: Boolean,
  val preloadedProfile: ProfileView? = null,
)
