package sh.christian.ozone.oauth

import sh.christian.ozone.oauth.dpop.DpopKeyPair
import kotlin.time.Duration

/**
 * Represents an OAuth token received after a successful authorization or refresh request.
 *
 * @param accessToken The access token used to authenticate API requests.
 * @param refreshToken The refresh token used to obtain a new access token when the current one expires.
 * @param keyPair The DPoP key pair used for signing requests.
 * @param expiresIn The duration for which the access token is valid.
 * @param scopes The list of scopes granted to the access token.
 * @param nonce A unique string to prevent replay attacks, typically used in conjunction with DPoP.
 */
data class OAuthToken(
  val accessToken: String,
  val refreshToken: String,
  val keyPair: DpopKeyPair,
  val expiresIn: Duration,
  val scopes: List<OAuthScope>,
  val nonce: String,
)
