package sh.christian.ozone.oauth

import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * Represents an OAuth authorization request.
 *
 * @param authorizeRequestUrl The URL to which the user should be redirected to authorize the request.
 * @param expiresIn The duration for which the authorization request is valid.
 * @param codeVerifier A unique string that will be used to verify the token request.
 * @param state A unique string to maintain state between the request and callback.
 * @param nonce A unique string to prevent replay attacks.
 */
@Serializable
data class OAuthAuthorizationRequest(
  val authorizeRequestUrl: String,
  val expiresIn: Duration,
  val codeVerifier: String,
  val state: String,
  val nonce: String,
)
