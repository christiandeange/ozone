package sh.christian.ozone.api.xrpc

import io.ktor.client.call.HttpClientCall
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.InternalAPI
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import kotlin.coroutines.CoroutineContext

internal class SimpleHttpResponse(
  override val call: HttpClientCall,
  override val status: HttpStatusCode,
  override val version: HttpProtocolVersion,
  override val requestTime: GMTDate,
  override val responseTime: GMTDate,
  override val headers: Headers,
  override val coroutineContext: CoroutineContext,
  @OptIn(InternalAPI::class)
  override val content: ByteReadChannel,
) : HttpResponse() {
  override fun toString(): String = "HttpResponse[${request.url}, $status]"
}

internal fun HttpResponse.copy(
  call: HttpClientCall = this.call,
  status: HttpStatusCode = this.status,
  version: HttpProtocolVersion = this.version,
  requestTime: GMTDate = this.requestTime,
  responseTime: GMTDate = this.responseTime,
  headers: Headers = this.headers,
  coroutineContext: CoroutineContext = this.coroutineContext,
  @OptIn(InternalAPI::class)
  content: ByteReadChannel = this.content,
): HttpResponse {
  return SimpleHttpResponse(
    call, status, version, requestTime, responseTime, headers, coroutineContext, content
  )
}
