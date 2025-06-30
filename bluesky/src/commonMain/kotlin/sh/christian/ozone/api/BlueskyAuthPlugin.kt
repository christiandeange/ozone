package sh.christian.ozone.api

import com.atproto.server.RefreshSessionResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.save
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.util.AttributeKey
import io.ktor.utils.io.KtorDsl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import sh.christian.ozone.BlueskyJson
import sh.christian.ozone.api.response.AtpErrorDescription
import sh.christian.ozone.api.xrpc.toAtpResponse

/**
 * Appends the `Authorization` header to XRPC requests, as well as automatically refreshing and
 * replaying a network request if it fails due to an expired access token.
 */
class BlueskyAuthPlugin(
  private val json: Json,
  private val authTokens: MutableStateFlow<Tokens?>,
) {
  @KtorDsl
  class Config(
    var json: Json = BlueskyJson,
    var authTokens: MutableStateFlow<Tokens?> = MutableStateFlow(null),
  )

  sealed interface Tokens {
    data class Bearer(
      val auth: String,
      val refresh: String,
    ) : Tokens

    data class Dpop(
      val dpop: String,
      val auth: String,
      val refresh: String,
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
      return BlueskyAuthPlugin(config.json, config.authTokens)
    }

    override fun install(
      plugin: BlueskyAuthPlugin,
      scope: HttpClient,
    ) {
      scope.plugin(HttpSend).intercept { context ->
        val tokens: Tokens? = plugin.authTokens.value
        if (!context.headers.contains(Authorization)) {
          context.auth(tokens)
        }

        var result: HttpClientCall = execute(context)

        if (result.response.status != BadRequest) {
          return@intercept result
        }

        // Cache the response in memory since we will need to decode it potentially more than once.
        result = result.save()

        val response = runCatching<AtpErrorDescription> {
          plugin.json.decodeFromString(result.response.bodyAsText())
        }

        if (response.getOrNull()?.error == "ExpiredToken" && tokens is Tokens.Bearer) {
          val refreshResponse = scope.post("/xrpc/com.atproto.server.refreshSession") {
            refresh(tokens)
          }

          refreshResponse.toAtpResponse<RefreshSessionResponse>().maybeResponse()?.let { refreshed ->
            val newAccessToken = refreshed.accessJwt
            val newRefreshToken = refreshed.refreshJwt

            val newTokens = Tokens.Bearer(newAccessToken, newRefreshToken)
            plugin.authTokens.value = newTokens

            context.headers.remove(Authorization)
            context.auth(newTokens)
            result = execute(context)
          }
        }

        result
      }
    }

    private fun HttpMessageBuilder.auth(tokens: Tokens?) {
      when (tokens) {
        is Tokens.Bearer -> {
          header(Authorization, tokens.auth)
        }
        is Tokens.Dpop -> {
          header(Authorization, "DPoP ${tokens.auth}")
          header("DPoP", tokens.dpop)
        }
        null -> {
          // No tokens available, do not add Authorization header
        }
      }
    }

    private fun HttpMessageBuilder.refresh(tokens: Tokens?) {
      when (tokens) {
        is Tokens.Bearer -> {
          header(Authorization, tokens.refresh)
        }
        is Tokens.Dpop -> {
          header(Authorization, "DPoP ${tokens.refresh}")
          header("DPoP", tokens.dpop)
        }
        null -> {
          // No tokens available, do not add Authorization header
        }
      }
    }
  }
}
