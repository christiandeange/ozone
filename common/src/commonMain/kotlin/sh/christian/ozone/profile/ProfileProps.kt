package sh.christian.ozone.profile

import app.bsky.actor.ProfileView

data class ProfileProps(
  val handle: String,
  val isSelf: Boolean,
  val preloadedProfile: ProfileView? = null,
)
