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
 * Use of [deleteSession] will clear the session. The session can also be manually cleared by calling [clearCredentials]
 * which will forget, but not invalidate, the current session tokens.
 */
class AuthenticatedXrpcBlueskyApi
private constructor(
  private val delegate: XrpcBlueskyApi,
  private val _authTokens: MutableStateFlow<BlueskyAuthPlugin.Tokens?>,
) : BlueskyApi by delegate {
  /**
   * The current session tokens. This will be `null` if the user is not authenticated.
   */
  val authTokens: StateFlow<BlueskyAuthPlugin.Tokens?> get() = _authTokens.asStateFlow()

  private constructor(
    httpClient: HttpClient,
    authTokens: MutableStateFlow<BlueskyAuthPlugin.Tokens?>,
  ) : this(
    delegate = XrpcBlueskyApi(httpClient.withBlueskyAuth(authTokens)),
    _authTokens = authTokens,
  )

  /**
   * Creates a new instance of [AuthenticatedXrpcBlueskyApi]
   *
   * @param httpClient Optional [HttpClient] to use for network requests. This instance will be configured to work with
   * basic XRPC API instances.
   * @param initialTokens Optional initial session tokens to use. If not provided, the API will start unauthenticated.
   */
  constructor(
    httpClient: HttpClient = defaultHttpClient,
    initialTokens: BlueskyAuthPlugin.Tokens? = null,
  ) : this(httpClient, MutableStateFlow(initialTokens))

  override suspend fun createAccount(request: CreateAccountRequest): AtpResponse<CreateAccountResponse> {
    return delegate.createAccount(request).also {
      it.saveTokens({ accessJwt }, { refreshJwt })
    }
  }

  override suspend fun createSession(request: CreateSessionRequest): AtpResponse<CreateSessionResponse> {
    return delegate.createSession(request).also {
      it.saveTokens({ accessJwt }, { refreshJwt })
    }
  }

  override suspend fun refreshSession(): AtpResponse<RefreshSessionResponse> {
    return delegate.refreshSession().also {
      it.saveTokens({ accessJwt }, { refreshJwt })
    }
  }

  override suspend fun deleteSession(): AtpResponse<Unit> {
    return delegate.deleteSession().also {
      if (it is AtpResponse.Success) {
        clearCredentials()
      }
    }
  }

  /**
   * Clears the current session tokens, effectively logging the user out. Note that the session and refresh token are
   * **not invalidated** on the server side, unlike calling [deleteSession].
   */
  fun clearCredentials() {
    _authTokens.value = null
  }

  private inline fun <reified T : Any> AtpResponse<T>.saveTokens(
    accessTokenProvider: T.() -> String,
    refreshTokenProvider: T.() -> String,
  ) {
    if (this is AtpResponse.Success) {
      _authTokens.value = BlueskyAuthPlugin.Tokens(
        auth = accessTokenProvider(response),
        refresh = refreshTokenProvider(response),
      )
    }
  }

  companion object {
    /**
     * Wraps an [XrpcBlueskyApi] instance as an [AuthenticatedXrpcBlueskyApi] with the optional initial tokens.
     */
    fun XrpcBlueskyApi.authenticated(initialTokens: BlueskyAuthPlugin.Tokens? = null): AuthenticatedXrpcBlueskyApi {
      return AuthenticatedXrpcBlueskyApi(this, MutableStateFlow(initialTokens))
    }

    private fun HttpClient.withBlueskyAuth(
      authTokens: MutableStateFlow<BlueskyAuthPlugin.Tokens?>,
    ): HttpClient {
      return config {
        install(BlueskyAuthPlugin) {
          this.authTokens = authTokens
        }
      }
    }
  }
}
