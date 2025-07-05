package sh.christian.ozone.oauth.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OAuthParRequest(
  @SerialName("response_type")
  val responseType: String,
  @SerialName("code_challenge_method")
  val codeChallengeMethod: String,
  @SerialName("scope")
  val scope: String,
  @SerialName("client_id")
  val clientId: String,
  @SerialName("redirect_uri")
  val redirectUri: String,
  @SerialName("code_challenge")
  val codeChallenge: String,
  @SerialName("state")
  val state: String,
  @SerialName("login_hint")
  val loginHint: String? = null,
)
