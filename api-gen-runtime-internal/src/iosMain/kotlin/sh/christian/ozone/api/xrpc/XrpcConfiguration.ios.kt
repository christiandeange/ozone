package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.DefaultRequest
import io.ktor.http.takeFrom

actual val defaultHttpClient: HttpClient = HttpClient(Darwin) {
  install(DefaultRequest) {
    url.takeFrom(BSKY_SOCIAL)
  }
}
