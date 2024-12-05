package sh.christian.ozone.api

import com.atproto.server.RefreshSessionResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.save
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders.Authorization
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

  data class Tokens(
    val auth: String,
    val refresh: String,
  )

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
        if (!context.headers.contains(Authorization)) {
          plugin.authTokens.value?.auth?.let { context.bearerAuth(it) }
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

        if (response.getOrNull()?.error == "ExpiredToken") {
          val refreshResponse = scope.post("/xrpc/com.atproto.server.refreshSession") {
            plugin.authTokens.value?.refresh?.let { bearerAuth(it) }
          }

          refreshResponse.toAtpResponse<RefreshSessionResponse>().maybeResponse()?.let { refreshed ->
            val newAccessToken = refreshed.accessJwt
            val newRefreshToken = refreshed.refreshJwt

            plugin.authTokens.value = Tokens(newAccessToken, newRefreshToken)

            context.headers.remove(Authorization)
            context.bearerAuth(newAccessToken)
            result = execute(context)
          }
        }

        result
      }
    }
  }
}
