package sh.christian.ozone.login.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthInfo(
  val accessJwt: String,
  val refreshJwt: String,
  val handle: String,
  val did: String,
)
