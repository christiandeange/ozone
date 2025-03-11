package sh.christian.ozone.oauth

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Url
import io.ktor.http.buildUrl
import io.ktor.http.takeFrom
import kotlinx.coroutines.coroutineScope
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.api.xrpc.defaultHttpClient
import sh.christian.ozone.api.xrpc.toAtpModel
import sh.christian.ozone.api.xrpc.toAtpResponse
import sh.christian.ozone.api.xrpc.withXrpcConfiguration
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation to build a URL that will interact with a hosted OAuth service.
 *
 * @constructor Construct a new OAuth API instance.
 *
 * @param client The HTTP client to use for requests.
 * @param challengeSelector The strategy to select the code challenge method.
 * @param random The random number generator to use for generating state and code challenges.
 */
class OAuthApi(
  private val client: HttpClient,
  private val challengeSelector: OAuthCodeChallengeMethodSelector,
  private val random: Random = CryptographicRandom(),
) {

  /** Construct a new instance using the Bluesky OAuth client and always using the `"S256"` code challenge method. */
  constructor() : this(
    client = defaultHttpClient.withXrpcConfiguration(),
    challengeSelector = OAuthCodeChallengeMethodSelector { OAuthCodeChallengeMethod.S256 },
    random = CryptographicRandom(),
  )

  /**
   * Build an authorization request for the given client and scopes.
   */
  suspend fun buildAuthorizationRequest(
    oauthClient: OAuthClient,
    scopes: List<OAuthScope>,
    loginHandleHint: String? = null,
  ): OAuthAuthorizationRequest = coroutineScope {
    val oauthServer = client.get("/.well-known/oauth-authorization-server").toAtpModel<OAuthAuthorizationServer>()

    val unsupportedScopes = scopes.filter { it.value !in oauthServer.scopesSupported }
    require(unsupportedScopes.isEmpty()) {
      "Requested scope(s) not supported by OAuth server: " +
          unsupportedScopes.joinToString(separator = ", ") { it.value }
    }

    val codeChallengeMethod = challengeSelector.selectCodeChallengeMethod(oauthServer.codeChallengeMethodsSupported)
    val codeVerifier = CharArray(64) { CODE_VERIFIER_CHARS.random(random) }.concatToString()
    val codeChallenge = codeChallengeMethod.provideCodeChallenge(codeVerifier)

    @OptIn(ExperimentalStdlibApi::class)
    val state = random.nextBytes(32).toHexString()

    val request = OAuthParRequest(
      responseType = "code",
      codeChallengeMethod = codeChallengeMethod.method,
      scope = scopes.joinToString(separator = " ") { it.value },
      clientId = oauthClient.clientId,
      redirectUri = oauthClient.redirectUri,
      codeChallenge = codeChallenge,
      state = state,
      loginHint = loginHandleHint,
    )

    val callResponse = client.post(Url(oauthServer.pushedAuthorizationRequestEndpoint)) {
      headers["Content-Type"] = "application/json"
      setBody(request)
    }

    val response = when (val responseType = callResponse.toAtpResponse<OAuthParResponse>()) {
      is AtpResponse.Success -> responseType.response
      is AtpResponse.Failure -> error(responseType.response ?: responseType.error ?: responseType.statusCode)
    }

    val nonce = requireNotNull(callResponse.headers["DPoP-Nonce"] ?: callResponse.headers["dpop-nonce"]) {
      "DPoP-Nonce header not found in authorization response"
    }

    val authorizeRequestUrl = buildUrl {
      takeFrom(oauthServer.authorizationEndpoint)
      parameters["request_uri"] = response.requestUri
      parameters["client_id"] = request.clientId
    }

    OAuthAuthorizationRequest(
      authorizeRequestUrl = authorizeRequestUrl,
      expiresIn = response.expiresIn.seconds,
      state = state,
      nonce = nonce,
    )
  }

  companion object {
    private val CODE_VERIFIER_CHARS: List<Char> =
      ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '.', '_', '~')
  }
}
