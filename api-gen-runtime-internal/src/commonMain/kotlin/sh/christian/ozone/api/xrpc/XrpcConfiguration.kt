package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import sh.christian.ozone.api.runtime.buildXrpcJsonConfiguration

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

fun HttpClient.withXrpcConfiguration(
  module: SerializersModule = Json.Default.serializersModule,
): HttpClient = config {
  install(ContentNegotiation) {
    json(buildXrpcJsonConfiguration(module))
  }

  install(WebSockets)
  install(WebsocketRedirectPlugin)
}
