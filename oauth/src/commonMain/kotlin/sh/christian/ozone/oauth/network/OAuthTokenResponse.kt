package sh.christian.ozone.oauth.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import sh.christian.ozone.api.Did

@Serializable
internal data class OAuthTokenResponse(
  @SerialName("access_token")
  val accessToken: String,
  @SerialName("token_type")
  val tokenType: String,
  @SerialName("expires_in")
  val expiresInSeconds: Int,
  @SerialName("refresh_token")
  val refreshToken: String,
  @SerialName("scope")
  val scopes: String,
  @SerialName("sub")
  val subject: Did,
)
