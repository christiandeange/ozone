package sh.christian.ozone.api

import com.atproto.server.CreateAccountRequest
import com.atproto.server.CreateAccountResponse
import com.atproto.server.CreateSessionRequest
import com.atproto.server.CreateSessionResponse
import com.atproto.server.RefreshSessionResponse
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import sh.christian.ozone.BlueskyApi
import sh.christian.ozone.XrpcBlueskyApi
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.api.xrpc.defaultHttpClient
import sh.christian.ozone.oauth.OAuthApi
import sh.christian.ozone.oauth.OAuthCodeChallengeMethod
import sh.christian.ozone.oauth.OAuthToken

data class AuthenticatedXrpcBlueskyApiHooks(
  val onSaveBearerTokens: (bearerTokens: BlueskyAuthPlugin.Tokens.Bearer) -> Unit = {},
  val onClearCredentials: () -> Unit = {},
)

/**
 * Wrapper around [XrpcBlueskyApi] to transparently manage session tokens on the user's behalf.
 *
 * This class is responsible for saving and restoring session tokens from the server's responses. The session will also
 * automatically be refreshed and retried using the refresh token if any method call results in an `ExpiredToken` error.
 * Use of one of the following methods will automatically start a session and save the tokens:
 * - [createAccount]
 * - [createSession]
 * - [refreshSession]
 *
 * A session can also be manually activated by invoking the [activateBearerTokens] or [activateOAuth] methods.
 *
 * Use of [deleteSession] (for Bearer auth) or [deleteOAuthSession] (for OAuth) will clear the session. The session
 * can also be manually cleared by calling [clearCredentials] which will remove, but not invalidate, the current session
 * tokens.
 */
class AuthenticatedXrpcBlueskyApi
private constructor(
  private val delegate: XrpcBlueskyApi,
  private val _authTokens: MutableStateFlow<BlueskyAuthPlugin.Tokens?>,
  private val _oauthApi: OAuthApi,
  private val _hooks: AuthenticatedXrpcBlueskyApiHooks = AuthenticatedXrpcBlueskyApiHooks(),
) : BlueskyApi by delegate {
  /**
   * The current session tokens. This will be `null` if the user is not authenticated.
   */
  val authTokens: StateFlow<BlueskyAuthPlugin.Tokens?> get() = _authTokens.asStateFlow()

  private constructor(
    httpClient: HttpClient,
    authTokens: MutableStateFlow<BlueskyAuthPlugin.Tokens?>,
    oauthApi: OAuthApi,
    hooks: AuthenticatedXrpcBlueskyApiHooks,
  ) : this(
    delegate = XrpcBlueskyApi(httpClient.withBlueskyAuth(authTokens, oauthApi)),
    _authTokens = authTokens,
    _oauthApi = oauthApi,
    _hooks = hooks,
  )

  /**
   * Creates a new instance of [AuthenticatedXrpcBlueskyApi]
   *
   * @param httpClient Optional [HttpClient] to use for network requests. This instance will be configured to work with
   * basic XRPC API instances.
   * @param initialTokens Optional initial session tokens to use. If not provided, the API will start unauthenticated.
   * @param oauthApi Optional [OAuthApi] instance to use for OAuth token management.
   */
  constructor(
    httpClient: HttpClient = defaultHttpClient,
    initialTokens: BlueskyAuthPlugin.Tokens? = null,
    oauthApi: OAuthApi = OAuthApi(httpClient, { OAuthCodeChallengeMethod.S256 }),
    hooks: AuthenticatedXrpcBlueskyApiHooks = AuthenticatedXrpcBlueskyApiHooks(),
  ) : this(httpClient, MutableStateFlow(initialTokens), oauthApi, hooks)

  override suspend fun createAccount(request: CreateAccountRequest): AtpResponse<CreateAccountResponse> {
    return delegate.createAccount(request).also {
      it.saveBearerTokens({ accessJwt }, { refreshJwt })
    }
  }

  override suspend fun createSession(request: CreateSessionRequest): AtpResponse<CreateSessionResponse> {
    return delegate.createSession(request).also {
      it.saveBearerTokens({ accessJwt }, { refreshJwt })
    }
  }

  override suspend fun refreshSession(): AtpResponse<RefreshSessionResponse> {
    return delegate.refreshSession().also {
      it.saveBearerTokens({ accessJwt }, { refreshJwt })
    }
  }

  override suspend fun deleteSession(): AtpResponse<Unit> {
    return delegate.deleteSession().also {
      if (it is AtpResponse.Success) {
        clearCredentials()
      }
      _hooks.onClearCredentials()
    }
  }

  /**
   * Activates the Bearer session using the provided tokens. This will save the access and refresh tokens. Rotation of
   * the tokens is handled automatically.
   */
  fun activateBearerTokens(
    accessToken: String,
    refreshToken: String,
  ) {
    _authTokens.value = BlueskyAuthPlugin.Tokens.Bearer(
      auth = accessToken,
      refresh = refreshToken,
    )
  }

  /**
   * Activates the OAuth session using the provided [OAuthToken]. This will save the OAuth access and refresh tokens,
   * as well as the PDS URL, key pair, client ID, and nonce. Rotation of the DPoP nonce is handled automatically.
   */
  fun activateOAuth(oauthToken: OAuthToken) {
    _authTokens.value = BlueskyAuthPlugin.Tokens.Dpop(
      auth = oauthToken.accessToken,
      refresh = oauthToken.refreshToken,
      pdsUrl = oauthToken.pds,
      keyPair = oauthToken.keyPair,
      clientId = oauthToken.clientId,
      nonce = oauthToken.nonce,
    )
  }

  /**
   * Revoke the current [oauthToken][OAuthToken] using the OAuth server's revocation endpoint.
   */
  suspend fun deleteOAuthSession() {
    val dpopToken = checkNotNull(_authTokens.value as? BlueskyAuthPlugin.Tokens.Dpop) {
      "Cannot delete OAuth session without DPoP tokens"
    }

    _oauthApi.revokeToken(
      accessToken = dpopToken.auth,
      clientId = dpopToken.clientId,
      nonce = dpopToken.nonce,
      keyPair = dpopToken.keyPair,
    )

    clearCredentials()
    _hooks.onClearCredentials()
  }

  /**
   * Clears the current session tokens, effectively logging the user out. Note that any previously-cached tokens are
   * **not invalidated** on the server side, unlike calling [deleteSession].
   */
  fun clearCredentials() {
    _authTokens.value = null
  }

  private inline fun <reified T : Any> AtpResponse<T>.saveBearerTokens(
    accessTokenProvider: T.() -> String,
    refreshTokenProvider: T.() -> String,
  ) {
    if (this is AtpResponse.Success) {
      val tokens = BlueskyAuthPlugin.Tokens.Bearer(
        auth = accessTokenProvider(response),
        refresh = refreshTokenProvider(response),
      )
      _authTokens.value = tokens
      _hooks.onSaveBearerTokens(tokens)
    }
  }

  companion object {
    /**
     * Wraps an [XrpcBlueskyApi] instance as an [AuthenticatedXrpcBlueskyApi] with the optional initial tokens.
     */
    fun XrpcBlueskyApi.authenticated(
      initialTokens: BlueskyAuthPlugin.Tokens? = null,
      oauthApi: OAuthApi = OAuthApi(client, { OAuthCodeChallengeMethod.S256 }),
    ): AuthenticatedXrpcBlueskyApi {
      return AuthenticatedXrpcBlueskyApi(this, MutableStateFlow(initialTokens), oauthApi)
    }

    private fun HttpClient.withBlueskyAuth(
      authTokens: MutableStateFlow<BlueskyAuthPlugin.Tokens?>,
      oauthApi: OAuthApi,
    ): HttpClient {
      return config {
        install(BlueskyAuthPlugin) {
          this.authTokens = authTokens
          this.oauthApi = oauthApi
        }
      }
    }
  }
}
