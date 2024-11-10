package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal val BSKY_SOCIAL = Url("https://bsky.social")
internal val BSKY_NETWORK = Url("https://bsky.network")

internal object WebsocketRedirectPlugin : ClientPlugin<Unit>
by createClientPlugin("WebsocketRedirect", {
  on(SendingRequest) { request, _ ->
    if (request.url.protocol == URLProtocol.WS || request.url.protocol == URLProtocol.WSS) {
      if (request.url.host == BSKY_SOCIAL.host) {
        request.url.host = BSKY_NETWORK.host
      }
    }
  }
})

expect val defaultHttpClient: HttpClient

fun HttpClient.withXrpcConfiguration(): HttpClient = config {
  val jsonEnvironment = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "${'$'}type"
  }

  install(ContentNegotiation) {
    json(jsonEnvironment)
  }

  install(WebSockets)
  install(WebsocketRedirectPlugin)
}
