package sh.christian.ozone.oauth.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OAuthParResponse(
  @SerialName("request_uri")
  val requestUri: String,
  @SerialName("expires_in")
  val expiresIn: Int,
)
