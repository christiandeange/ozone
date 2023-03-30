package sh.christian.ozone.profile

import sh.christian.ozone.model.Profile
import sh.christian.ozone.user.UserReference

data class ProfileProps(
  val user: UserReference,
  val isSelf: Boolean,
  val preloadedProfile: Profile? = null,
)
