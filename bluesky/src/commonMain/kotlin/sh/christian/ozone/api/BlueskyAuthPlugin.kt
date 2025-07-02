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
        if (!context.headers.contains(Authorization)) {
          context.auth(plugin)
        }

        var result: HttpClientCall = execute(context)

        if (result.response.status.isSuccess()) {
          return@intercept result
        }

        // Cache the response in memory since we will need to decode it potentially more than once.
        result = result.save()

        val response = runCatching<AtpErrorDescription> {
          plugin.json.decodeFromString(result.response.bodyAsText())
        }

        val newTokens = when (response.getOrNull()?.error) {
          "ExpiredToken" -> refreshExpiredToken(plugin, scope)
          "use_dpop_nonce" -> refreshDpopNonce(plugin, result.response)
          else -> null
        }

        if (newTokens != null) {
          plugin.authTokens.value = newTokens

          context.headers.remove(Authorization)
          context.headers.remove("DPoP")
          context.auth(plugin)
          result = execute(context)
        }

        onResponse(plugin, result.response)
        result
      }
    }

    private suspend fun HttpRequestBuilder.auth(plugin: BlueskyAuthPlugin) {
      when (val tokens = plugin.authTokens.value) {
        is Tokens.Bearer -> header(Authorization, "Bearer ${tokens.auth}")
        is Tokens.Dpop -> applyDpop(plugin, tokens, tokens.auth)
        null -> {
          // No tokens available, do not add Authorization header
        }
      }
    }

    private suspend fun HttpRequestBuilder.refresh(plugin: BlueskyAuthPlugin) {
      when (val tokens = plugin.authTokens.value) {
        is Tokens.Bearer -> header(Authorization, "Bearer ${tokens.refresh}")
        is Tokens.Dpop -> applyDpop(plugin, tokens, tokens.refresh)
        null -> {
          // No tokens available, do not add Authorization header
        }
      }
    }

    private fun onResponse(
      plugin: BlueskyAuthPlugin,
      response: HttpResponse,
    ) {
      refreshDpopNonce(plugin, response)?.let { newTokens ->
        plugin.authTokens.value = newTokens
      }
    }

    private suspend fun refreshExpiredToken(
      plugin: BlueskyAuthPlugin,
      scope: HttpClient,
    ): Tokens? {
      return when (val tokens = plugin.authTokens.value) {
        is Tokens.Bearer -> {
          val refreshResponse = scope.post("/xrpc/com.atproto.server.refreshSession") {
            refresh(plugin)
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
          val nonce = callResponse.headers["DPoP-Nonce"] ?: callResponse.headers["dpop-nonce"]
          nonce?.let { tokens.copy(nonce = nonce) }
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
        clientId = tokens.clientId,
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
