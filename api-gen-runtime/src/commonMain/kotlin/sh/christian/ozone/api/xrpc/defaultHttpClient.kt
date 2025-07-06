package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.http.takeFrom

/**
 * Platform-default HTTP client for network requests.
 *
 * This client is configured to use [Bluesky Social][BSKY_SOCIAL] by default.
 */
val defaultHttpClient: HttpClient = HttpClient(defaultHttpEngine) {
  install(DefaultRequest) {
    url.takeFrom(BSKY_SOCIAL)
  }
}
