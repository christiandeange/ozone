package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.DefaultRequest
import io.ktor.http.takeFrom

actual val defaultHttpClient: HttpClient = HttpClient(Js) {
  install(DefaultRequest) {
    url.takeFrom("https://bsky.social")
  }
}

