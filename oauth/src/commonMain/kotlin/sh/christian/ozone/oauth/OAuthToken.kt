package sh.christian.ozone.oauth

import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.buildUrl
import io.ktor.util.decodeBase64String
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import sh.christian.ozone.api.Did
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
 * @param subject The DID of the user account associated with the token.
 * @param nonce A unique string to prevent replay attacks, typically used in conjunction with DPoP.
 */
data class OAuthToken(
  val accessToken: String,
  val refreshToken: String,
  val keyPair: DpopKeyPair,
  val expiresIn: Duration,
  val scopes: List<OAuthScope>,
  val subject: Did,
  val nonce: String,
) {
  private val payloadJwt: String by lazy {
    accessToken.split(".")[1]
  }
  private val payloadJson: JsonObject by lazy {
    Json.decodeFromString(JsonObject.serializer(), payloadJwt.decodeBase64String())
  }

  /**
   * The unique identifier for the OAuth client.
   */
  val clientId: String = requirePayload("client_id")

  /**
   * The audience of the JWT, typically the DID of the PDS (Personal Data Server) that the token is intended for.
   */
  val audience: Did = Did(requirePayload("aud"))

  /**
   * The URL of the PDS (Personal Data Server) associated with the audience.
   */
  val pds: Url = buildUrl {
    protocol = URLProtocol.HTTPS
    host = audience.toString().substringAfterLast(":")
  }

  private fun requirePayload(key: String): String {
    return requireNotNull(payloadJson[key]?.let { (it as? JsonPrimitive)?.contentOrNull }) {
      "JWT payload does not contain '$key' claim"
    }
  }
}
