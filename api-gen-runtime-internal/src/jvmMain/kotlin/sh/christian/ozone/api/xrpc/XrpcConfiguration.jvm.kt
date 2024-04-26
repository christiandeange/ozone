package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.http.takeFrom

actual val defaultHttpClient: HttpClient = HttpClient(CIO) {
  install(DefaultRequest) {
    url.takeFrom(BSKY_SOCIAL)
  }

  install(WebsocketRedirectPlugin)
}
