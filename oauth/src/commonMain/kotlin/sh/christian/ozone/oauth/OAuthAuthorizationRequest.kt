package sh.christian.ozone.oauth

import io.ktor.http.Url
import kotlin.time.Duration

/**
 * Represents an OAuth authorization request.
 *
 * @param authorizeRequestUrl The URL to which the user should be redirected to authorize the request.
 * @param expiresIn The duration for which the authorization request is valid.
 * @param state A unique string to maintain state between the request and callback.
 * @param nonce A unique string to prevent replay attacks.
 */
data class OAuthAuthorizationRequest(
  val authorizeRequestUrl: Url,
  val expiresIn: Duration,
  val state: String,
  val nonce: String,
)
