package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.save
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.serialization.kotlinx.json.DefaultJson
import io.ktor.util.AttributeKey
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import sh.christian.ozone.api.response.AtpErrorDescription

/**
 * Fixes an incompatibility with the Bluesky XRPC server and the Ktor Auth plugin.
 *
 * The plugin requires an HTTP 401 in order to determine that the auth tokens should be refreshed,
 * however the XRPC server returns a 400 with the error description body containing an `error` of
 * `"ExpiredToken"`. We need to intercept that response and propagate a 401 status code instead
 * to trigger the refresh flow.
 */
internal class FixExpiredTokenErrorPlugin(private val json: Json) {
  class Config(
    var json: Json = DefaultJson
  )

  companion object : HttpClientPlugin<Config, FixExpiredTokenErrorPlugin> {
    override val key = AttributeKey<FixExpiredTokenErrorPlugin>("FixExpiredTokenErrorPlugin")

    override fun prepare(block: Config.() -> Unit): FixExpiredTokenErrorPlugin {
      return FixExpiredTokenErrorPlugin(Config().apply(block).json)
    }

    override fun install(
      plugin: FixExpiredTokenErrorPlugin,
      scope: HttpClient
    ) {
      scope.plugin(HttpSend).intercept { context ->
        val result: HttpClientCall = execute(context)
        if (result.response.status != BadRequest) return@intercept result

        val peekResult: HttpClientCall = result.save()
        val response = runCatching<AtpErrorDescription> {
          plugin.json.decodeFromString(peekResult.response.bodyAsText())
        }

        if (response.getOrNull()?.error == "ExpiredToken") {
          SimpleHttpCall(
            client = peekResult.client,
            request = peekResult.request,
            response = peekResult.response.copy(status = Unauthorized),
          )
        } else {
          peekResult
        }
      }
    }
  }
}
