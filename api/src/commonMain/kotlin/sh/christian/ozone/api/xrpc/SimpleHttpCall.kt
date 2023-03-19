package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.HttpRequest
import io.ktor.client.statement.HttpResponse

internal class SimpleHttpCall(
  client: HttpClient,
  request: HttpRequest,
  response: HttpResponse,
) : HttpClientCall(client) {
  init {
    this.request = request
    this.response = response
  }
}
