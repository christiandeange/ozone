package sh.christian.ozone.api

import com.atproto.server.RefreshSessionResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.save
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.Url
import io.ktor.http.isSuccess
import io.ktor.util.AttributeKey
import io.ktor.utils.io.KtorDsl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import sh.christian.ozone.BlueskyJson
import sh.christian.ozone.api.response.AtpErrorDescription
import sh.christian.ozone.api.xrpc.toAtpResponse
import sh.christian.ozone.oauth.DpopKeyPair
import sh.christian.ozone.oauth.OAuthApi

/**
 * Appends the `Authorization` header to XRPC requests, as well as automatically refreshing and
 * replaying a network request if it fails due to an expired access token.
 */
class BlueskyAuthPlugin(
  private val json: Json,
  private val oauthApi: OAuthApi,
  private val authTokens: MutableStateFlow<Tokens?>,
) {
  /**
   * Serializes all token refreshes and DPoP nonce rotations. DPoP refresh tokens are single-use and the server rotates
   * the nonce on every response, so concurrent requests must not refresh in parallel or they will clobber each other's
   * tokens and loop on `use_dpop_nonce`.
   */
  private val refreshMutex = Mutex()

  @KtorDsl
  class Config(
    var json: Json = BlueskyJson,
    var oauthApi: OAuthApi = OAuthApi(),
    var authTokens: MutableStateFlow<Tokens?> = MutableStateFlow(null),
  )

  sealed interface Tokens {

    /**
     * Represents a set of authentication bearer tokens used for accessing the Bluesky API. These are typically acquired
     * by logging into Bluesky using a username and password combination.
     */
    data class Bearer(
      val auth: String,
      val refresh: String,
    ) : Tokens

    /**
     * Represents a set of DPoP authentication tokens used for accessing the Bluesky API. These are typically acquired
     * by authenticating with an OAuth client against the Bluesky OAuth server. Network requests using DPoP will be
     * automatically rerouted directly to the PDS.
     */
    data class Dpop(
      val auth: String,
      val refresh: String,
      val pdsUrl: Url,
      val keyPair: DpopKeyPair,
      val clientId: String,
      val nonce: String,
    ) : Tokens

    companion object {
      @Deprecated(
        "Use BlueskyAuthPlugin.Tokens.Bearer constructor directly instead",
        replaceWith = ReplaceWith(expression = "BlueskyAuthPlugin.Tokens.Bearer"),
      )
      operator fun invoke(
        auth: String,
        refresh: String,
      ) = Bearer(auth, refresh)
    }
  }

  companion object : HttpClientPlugin<Config, BlueskyAuthPlugin> {
    override val key = AttributeKey<BlueskyAuthPlugin>("BlueskyAuthPlugin")

    override fun prepare(block: Config.() -> Unit): BlueskyAuthPlugin {
      val config = Config().apply(block)
      return BlueskyAuthPlugin(config.json, config.oauthApi, config.authTokens)
    }

    override fun install(
      plugin: BlueskyAuthPlugin,
      scope: HttpClient,
    ) {
      scope.plugin(HttpSend).intercept { context ->
        // If the http client already had an Authorization header, do not attempt to add our own Authorization header or
        // handle token-related errors. The assumption is that the caller is managing token authentication themselves.
        val alreadyHasAuth = context.headers.contains(Authorization)
        var retryable = !alreadyHasAuth

        // Snapshot of the tokens used to authenticate the current attempt. Refreshes are keyed off this value so that
        // concurrent requests can detect when another coroutine has already refreshed and reuse its result.
        var tokensUsed = plugin.authTokens.value

        if (!alreadyHasAuth) {
          context.auth(plugin, tokensUsed)
        }

        var result: HttpClientCall = execute(context)

        while (!result.response.status.isSuccess() && retryable) {
          // Cache the response in memory since we will need to decode it potentially more than once.
          result = result.save()

          val response = runCatching<AtpErrorDescription> {
            plugin.json.decodeFromString(result.response.bodyAsText())
          }

          val newTokens = when (response.getOrNull()?.error) {
            "ExpiredToken",
            "InvalidToken",
            "invalid_token" -> maybeRefreshToken(plugin, scope, tokensUsed)
            "use_dpop_nonce" -> maybeRefreshDpopNonce(plugin, result.response, tokensUsed)
            else -> null
          }

          // Only retry if the refresh produced different tokens; otherwise we would loop on the same failing request.
          retryable = newTokens != null && newTokens != tokensUsed

          if (retryable) {
            tokensUsed = newTokens

            // Apply the new authentication tokens to the request and retry.
            context.headers.remove(Authorization)
            context.headers.remove("DPoP")
            context.auth(plugin, newTokens)
            result = execute(context)
          }
        }

        // Skip when the caller manages their own auth: not only is there nothing to rotate, but this also covers the
        // nested refresh request (which carries its own Authorization header) that runs while the refresh mutex is held,
        // avoiding a re-entrant lock acquisition on the non-reentrant mutex.
        if (!alreadyHasAuth) {
          onResponse(plugin, result.response, tokensUsed)
        }
        result
      }
    }

    private suspend fun HttpRequestBuilder.auth(
      plugin: BlueskyAuthPlugin,
      tokens: Tokens?,
    ) {
      when (tokens) {
        is Tokens.Bearer -> header(Authorization, "Bearer ${tokens.auth}")
        is Tokens.Dpop -> applyDpop(plugin, tokens, tokens.auth)
        null -> {
          // No tokens available, do not add Authorization header
        }
      }
    }

    private suspend fun HttpRequestBuilder.refresh(
      plugin: BlueskyAuthPlugin,
      tokens: Tokens?,
    ) {
      when (tokens) {
        is Tokens.Bearer -> header(Authorization, "Bearer ${tokens.refresh}")
        is Tokens.Dpop -> applyDpop(plugin, tokens, tokens.refresh)
        null -> {
          // No tokens available, do not add Authorization header
        }
      }
    }

    private suspend fun onResponse(
      plugin: BlueskyAuthPlugin,
      response: HttpResponse,
      tokensUsed: Tokens?,
    ) {
      // Capture a rotated DPoP nonce from a successful response, if present.
      maybeRefreshDpopNonce(plugin, response, tokensUsed)
    }

    /**
     * Refreshes an expired access token, serialized so that at most one refresh runs at a time. If another coroutine
     * already refreshed the tokens (detected by comparing against [staleTokens]), its result is reused instead of
     * performing another refresh.
     */
    private suspend fun maybeRefreshToken(
      plugin: BlueskyAuthPlugin,
      scope: HttpClient,
      staleTokens: Tokens?,
    ): Tokens? = plugin.refreshMutex.withLock {
      val current = plugin.authTokens.value
      if (current != staleTokens) {
        // Another coroutine already refreshed the tokens! Use these without making another network call.
        current
      } else {
        unsafeRefreshToken(plugin, scope)?.also { plugin.authTokens.value = it }
      }
    }

    private suspend fun unsafeRefreshToken(
      plugin: BlueskyAuthPlugin,
      scope: HttpClient,
    ): Tokens? {
      return when (val tokens = plugin.authTokens.value) {
        is Tokens.Bearer -> {
          val refreshResponse = scope.post("/xrpc/com.atproto.server.refreshSession") {
            refresh(plugin, tokens)
          }

          refreshResponse.toAtpResponse<RefreshSessionResponse>().maybeResponse()?.let { refreshed ->
            Tokens.Bearer(
              auth = refreshed.accessJwt,
              refresh = refreshed.refreshJwt,
            )
          }
        }
        is Tokens.Dpop -> {
          plugin.oauthApi.refreshToken(
            clientId = tokens.clientId,
            nonce = tokens.nonce,
            refreshToken = tokens.refresh,
            keyPair = tokens.keyPair,
          ).let { refreshed ->
            Tokens.Dpop(
              auth = refreshed.accessToken,
              refresh = refreshed.refreshToken,
              pdsUrl = refreshed.pds,
              keyPair = refreshed.keyPair,
              clientId = refreshed.clientId,
              nonce = refreshed.nonce,
            )
          }
        }
        null -> {
          // No tokens available, unable to refresh
          null
        }
      }
    }

    /**
     * Rotates the DPoP nonce from the given response, serialized so that concurrent requests cannot clobber a token
     * that was refreshed in the meantime. The nonce is only applied if the existing tokens still match `tokensUsed`
     * (no other coroutine has refreshed them), otherwise the existing tokens are returned.
     */
    private suspend fun maybeRefreshDpopNonce(
      plugin: BlueskyAuthPlugin,
      callResponse: HttpResponse,
      tokensUsed: Tokens?,
    ): Tokens? = plugin.refreshMutex.withLock {
      val current = plugin.authTokens.value
      if (current != tokensUsed) {
        // Tokens were refreshed concurrently; the current tokens already carry a fresh nonce, so reuse them.
        current
      } else {
        refreshDpopNonce(plugin, callResponse)?.also { plugin.authTokens.value = it }
      }
    }

    private fun refreshDpopNonce(
      plugin: BlueskyAuthPlugin,
      callResponse: HttpResponse,
    ): Tokens? {
      return when (val tokens = plugin.authTokens.value) {
        is Tokens.Bearer -> {
          // Bearer tokens do not use DPoP, unable to refresh
          null
        }
        is Tokens.Dpop -> {
          callResponse.headers["DPoP-Nonce"]?.let { tokens.copy(nonce = it) }
        }
        null -> {
          // No tokens available, unable to refresh
          null
        }
      }
    }

    private suspend fun HttpRequestBuilder.applyDpop(
      plugin: BlueskyAuthPlugin,
      tokens: Tokens.Dpop,
      auth: String,
    ) {
      url.protocol = tokens.pdsUrl.protocol
      url.host = tokens.pdsUrl.host

      val dpopHeader = plugin.oauthApi.createDpopHeaderValue(
        keyPair = tokens.keyPair,
        method = method.value,
        endpoint = url.toString(),
        nonce = tokens.nonce,
        accessToken = auth,
      )

      header(Authorization, "DPoP $auth")
      header("DPoP", dpopHeader)
    }
  }
}
