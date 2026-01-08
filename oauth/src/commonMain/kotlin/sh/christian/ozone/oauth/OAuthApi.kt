package sh.christian.ozone.oauth

import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.random.CryptographyRandom
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.call.save
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.http.Url
import io.ktor.http.buildUrl
import io.ktor.http.headers
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import sh.christian.ozone.api.response.AtpErrorDescription
import sh.christian.ozone.api.response.AtpException
import sh.christian.ozone.api.response.StatusCode
import sh.christian.ozone.api.runtime.buildXrpcJsonConfiguration
import sh.christian.ozone.api.xrpc.defaultHttpClient
import sh.christian.ozone.oauth.network.OAuthAuthorizationServer
import sh.christian.ozone.oauth.network.OAuthParRequest
import sh.christian.ozone.oauth.network.OAuthParResponse
import sh.christian.ozone.oauth.network.OAuthTokenResponse
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation to build a URL that will interact with a hosted OAuth service.
 *
 * @constructor Construct a new OAuth API instance.
 *
 * @param httpClient The HTTP client to use for requests.
 * @param challengeSelector The strategy to select the code challenge method.
 * @param random The random number generator to use for generating state and code challenges.
 * @param clock The clock to use for generating timestamps.
 */
class OAuthApi(
  private val httpClient: HttpClient,
  private val challengeSelector: OAuthCodeChallengeMethodSelector,
  private val random: Random = CryptographyRandom,
  private val clock: Clock = Clock.System,
) {
  private val client: HttpClient = httpClient.config {
    if (httpClient.pluginOrNull(ContentNegotiation) == null) {
      install(ContentNegotiation) {
        json(buildXrpcJsonConfiguration())
      }
    }
  }

  private lateinit var oauthServer: OAuthAuthorizationServer
  private var dpopKeyPair: DpopKeyPair? = null

  /** Construct a new instance using the Bluesky OAuth client and always using the `"S256"` code challenge method. */
  constructor() : this(
    httpClient = defaultHttpClient,
    challengeSelector = OAuthCodeChallengeMethodSelector { OAuthCodeChallengeMethod.S256 },
    random = CryptographyRandom,
    clock = Clock.System,
  )

  /**
   * Build an authorization request for the given client and scopes. This URL can be used to redirect the user to
   * the OAuth server to log into their account.
   *
   * Keep track of the [OAuthAuthorizationRequest]'s [nonce][OAuthAuthorizationRequest.nonce] and
   * [codeVerifier][OAuthAuthorizationRequest.codeVerifier] to use later when requesting an access token.
   */
  suspend fun buildAuthorizationRequest(
    oauthClient: OAuthClient,
    scopes: List<OAuthScope>,
    loginHandleHint: String? = null,
  ): OAuthAuthorizationRequest = coroutineScope {
    val oauthServer = resolveOAuthAuthorizationServer()

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

    val parEndpoint = requireNotNull(oauthServer.pushedAuthorizationRequestEndpoint) {
      "OAuth server does not support Pushed Authorization Requests"
    }

    val callResponse = client.post(Url(parEndpoint)) {
      headers["Content-Type"] = "application/json"
      setBody(request)
    }

    val response: OAuthParResponse = callResponse.decodeResponse(
      onSuccess = { it },
      onNewNonce = { error("Should not happen") },
      onFailure = {
        val errorMessage = it?.toString() ?: StatusCode.fromCode(callResponse.status.value).toString()
        error("Failed to create PAR request: $errorMessage")
      },
    )

    val nonce = callResponse.responseDpopNonce

    val authorizeRequestUrl = buildUrl {
      takeFrom(oauthServer.authorizationEndpoint)
      parameters["request_uri"] = response.requestUri
      parameters["client_id"] = request.clientId
    }

    OAuthAuthorizationRequest(
      authorizeRequestUrl = authorizeRequestUrl.toString(),
      expiresIn = response.expiresIn.seconds,
      codeVerifier = codeVerifier,
      state = state,
      nonce = nonce,
    )
  }

  /**
   * Request an access token using the provided OAuth client, nonce, code verifier, and authorization code.
   *
   * You are **highly encouraged** to verify that the state received in the callback matches the state returned by
   * [buildAuthorizationRequest] before calling this method, to prevent CSRF attacks. You can also pass in an optional
   * [DPoP key pair][DpopKeyPair] to sign the request with DPoP, otherwise a new key pair will be generated for you.
   */
  suspend fun requestToken(
    oauthClient: OAuthClient,
    nonce: String,
    codeVerifier: String,
    code: String,
    keyPair: DpopKeyPair? = null,
  ): OAuthToken {
    val requestParameters = ParametersBuilder().apply {
      append("grant_type", "authorization_code")
      append("client_id", oauthClient.clientId)
      append("code", code)
      append("code_verifier", codeVerifier)
      append("redirect_uri", oauthClient.redirectUri)
    }.build()

    return requestOrRefreshToken(
      requestParameters = requestParameters,
      nonce = nonce,
      keyPair = keyPair,
    )
  }

  /** @see refreshToken */
  suspend fun refreshToken(
    oauthClient: OAuthClient,
    nonce: String?,
    refreshToken: String,
    keyPair: DpopKeyPair? = null,
  ): OAuthToken {
    return refreshToken(
      clientId = oauthClient.clientId,
      nonce = nonce,
      refreshToken = refreshToken,
      keyPair = keyPair,
    )
  }

  /**
   * Refresh an access token using the provided OAuth client, nonce, and refresh token.
   *
   * The nonce should be passed in from the previous [requestToken] request, if available.
   *
   * You can also pass in an optional [DPoP key pair][DpopKeyPair] to sign the request with DPoP, otherwise it will use
   * the one used for [requestToken]. If no key pair was used for the initial token request by this [OAuthApi] instance,
   * a new one will be generated for you.
   */
  suspend fun refreshToken(
    clientId: String,
    nonce: String?,
    refreshToken: String,
    keyPair: DpopKeyPair? = null,
  ): OAuthToken {
    val requestParameters = ParametersBuilder().apply {
      append("grant_type", "refresh_token")
      append("client_id", clientId)
      append("refresh_token", refreshToken)
    }.build()

    return requestOrRefreshToken(
      requestParameters = requestParameters,
      nonce = nonce,
      keyPair = keyPair,
    )
  }

  private suspend fun requestOrRefreshToken(
    requestParameters: Parameters,
    nonce: String?,
    keyPair: DpopKeyPair?
  ): OAuthToken {
    val oauthServer = resolveOAuthAuthorizationServer()
    val tokenRequestUrl = Url(oauthServer.tokenEndpoint)

    val dpopKeyPair = resolveDpopKeyPair(providedKeyPair = keyPair)

    val dpopHeader = createDpopHeaderValue(
      keyPair = dpopKeyPair,
      method = "POST",
      endpoint = tokenRequestUrl.toString(),
      nonce = nonce,
      accessToken = null,
    )

    val callResponse = client.post(tokenRequestUrl) {
      headers["DPoP"] = dpopHeader
      setBody(FormDataContent(requestParameters))
    }

    return callResponse.mapResponse<OAuthTokenResponse, OAuthToken>(
      onSuccess = { tokenResponse ->
        OAuthToken(
          accessToken = tokenResponse.accessToken,
          refreshToken = tokenResponse.refreshToken,
          keyPair = dpopKeyPair,
          expiresIn = tokenResponse.expiresInSeconds.seconds,
          scopes = tokenResponse.scopes.split(" ").map { OAuthScope(it) },
          subject = tokenResponse.subject,
          nonce = responseDpopNonce,
        )
      },
      onNewNonce = { newNonce ->
        // If the response indicates we need to use a new DPoP nonce, we can retry with the new nonce.
        requestOrRefreshToken(requestParameters, newNonce, dpopKeyPair)
      },
      onFailure = { errorDescription ->
        throw AtpException(StatusCode.fromCode(status.value), errorDescription)
      },
    )
  }

  /**
   * Revoke the provided [oauthToken][OAuthToken] using the OAuth server's revocation endpoint.
   */
  suspend fun revokeToken(oauthToken: OAuthToken) {
    revokeToken(
      accessToken = oauthToken.accessToken,
      clientId = oauthToken.clientId,
      nonce = oauthToken.nonce,
      keyPair = oauthToken.keyPair,
    )
  }

  /**
   * Revoke the provided access token using the OAuth server's revocation endpoint.
   *
   * The nonce should be passed in from the previous [requestToken] request, if available.
   *
   * You can also pass in an optional [DPoP key pair][DpopKeyPair] to sign the request with DPoP, otherwise it will use
   * the one used for [requestToken]. If no key pair was used for the initial token request by this [OAuthApi] instance,
   * a new one will be generated for you.
   */
  suspend fun revokeToken(
    accessToken: String,
    clientId: String,
    nonce: String?,
    keyPair: DpopKeyPair?,
  ) {
    val oauthServer = resolveOAuthAuthorizationServer()
    val revokeUrl = requireNotNull(oauthServer.revocationEndpoint) {
      "OAuth server does not support token revocation"
    }

    val dpopKeyPair = resolveDpopKeyPair(providedKeyPair = keyPair)

    val dpopHeader = createDpopHeaderValue(
      keyPair = dpopKeyPair,
      method = "POST",
      endpoint = revokeUrl,
      nonce = nonce,
      accessToken = null,
    )

    val callResponse = client.post(Url(revokeUrl)) {
      headers["Content-Type"] = "application/x-www-form-urlencoded"
      headers["Authorization"] = "DPoP $accessToken"
      headers["DPoP"] = dpopHeader

      setBody(
        FormDataContent(
          Parameters.build {
            append("token", accessToken)
            append("token_type_hint", "access_token")
            append("client_id", clientId)
          }
        )
      )
    }

    return callResponse.mapResponse<JsonObject, Unit>(
      onSuccess = { _ -> },
      onNewNonce = { newNonce ->
        revokeToken(accessToken, clientId, newNonce, dpopKeyPair)
      },
      onFailure = { errorDescription ->
        throw AtpException(StatusCode.fromCode(status.value), errorDescription)
      },
    )
  }

  /**
   * Create a value for the `DPoP` header to be used in requests to the OAuth server.
   */
  suspend fun createDpopHeaderValue(
    keyPair: DpopKeyPair,
    method: String,
    endpoint: String,
    nonce: String?,
    accessToken: String?,
  ): String {
    val rawBytes = keyPair.publicKey(DpopKeyPair.PublicKeyFormat.RAW)
    check(rawBytes.first() == 0x04.toByte()) {
      "Unexpected public key format: expected uncompressed (0x04) but got ${rawBytes.first()}"
    }
    val x = rawBytes.copyOfRange(1, 33)
    val y = rawBytes.copyOfRange(33, 65)

    val headerMap = buildJsonObject {
      put("typ", JsonPrimitive("dpop+jwt"))
      put("alg", JsonPrimitive("ES256"))
      put("jwk", buildJsonObject {
        put("crv", JsonPrimitive("P-256"))
        put("kty", JsonPrimitive("EC"))
        put("x", JsonPrimitive(x.encodeBase64Url()))
        put("y", JsonPrimitive(y.encodeBase64Url()))
      })
    }
    val claimsMap = buildJsonObject {
      put("iat", JsonPrimitive(clock.now().epochSeconds))
      put("jti", JsonPrimitive(random.nextBytes(16).encodeBase64Url()))
      put("htm", JsonPrimitive(method))
      put("htu", JsonPrimitive(endpoint))
      nonce?.let {
        put("nonce", JsonPrimitive(it))
      }
      accessToken?.let {
        put("ath", JsonPrimitive(OAuthCodeChallengeMethod.S256.provideCodeChallenge(accessToken)))
      }
    }

    val header = Json.encodeToString(headerMap).encodeToByteArray().encodeBase64Url()
    val claims = Json.encodeToString(claimsMap).encodeToByteArray().encodeBase64Url()
    val signedData = "$header.$claims"

    val signature = keyPair.keyPair.privateKey
      .signatureGenerator(digest = SHA256, format = ECDSA.SignatureFormat.RAW)
      .generateSignature(signedData.encodeToByteArray())
      .encodeBase64Url()

    return "$signedData.$signature"
  }

  private suspend fun resolveOAuthAuthorizationServer(): OAuthAuthorizationServer {
    if (!::oauthServer.isInitialized) {
      oauthServer = client.get("/.well-known/oauth-authorization-server").decodeResponse(
        onSuccess = { it },
        onNewNonce = { error("Should not happen") },
        onFailure = { errorDescription ->
          throw AtpException(StatusCode.fromCode(status.value), errorDescription)
        },
      )
    }
    return oauthServer
  }

  private suspend fun resolveDpopKeyPair(providedKeyPair: DpopKeyPair?): DpopKeyPair {
    // Use a provided DPoP key pair if provided, or the previously-used one if available, or generate a new one.
    return (providedKeyPair ?: dpopKeyPair ?: DpopKeyPair.generateKeyPair())
        .also { dpopKeyPair = it }
  }

  private suspend inline fun <reified T : Any> HttpResponse.decodeResponse(
    onSuccess: suspend HttpResponse.(T) -> T,
    onNewNonce: suspend HttpResponse.(String) -> T,
    onFailure: suspend HttpResponse.(AtpErrorDescription?) -> T,
  ): T {
    return mapResponse<T, T>(onSuccess, onNewNonce, onFailure)
  }

  private suspend inline fun <reified T : Any, R> HttpResponse.mapResponse(
    onSuccess: suspend HttpResponse.(T) -> R,
    onNewNonce: suspend HttpResponse.(String) -> R,
    onFailure: suspend HttpResponse.(AtpErrorDescription?) -> R,
  ): R {
    call.save()
    return if (status.isSuccess()) {
      onSuccess(body<T>())
    } else {
      val maybeErrorDescription = runCatching { body<AtpErrorDescription>() }.getOrNull()
      if (maybeErrorDescription?.error == "use_dpop_nonce") {
        // If the error indicates we need to use a DPoP nonce, we can retry with the new nonce.
        return onNewNonce(responseDpopNonce)
      } else {
        onFailure(maybeErrorDescription)
      }
    }
  }

  private val HttpResponse.responseDpopNonce: String
    get() = requireNotNull(headers["DPoP-Nonce"]) { "DPoP-Nonce header not found in response" }

  companion object {
    private val CODE_VERIFIER_CHARS: List<Char> =
      ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '.', '_', '~')
  }
}
