package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import sh.christian.ozone.api.Encoding
import sh.christian.ozone.api.response.AtpErrorDescription
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.api.response.StatusCode

internal suspend inline fun HttpClient.query(
  path: String,
  queryParams: Map<String, Any?>,
): HttpResponse {
  return get(path) {
    queryParams.forEach(::parameter)
  }
}

internal suspend inline fun <reified T : Any> HttpClient.procedure(
  path: String,
  body: T,
): HttpResponse {
  return post(path) {
    val annotation = body::class.annotations.filterIsInstance<Encoding>().firstOrNull()
    annotation?.type?.firstOrNull()?.let { encoding ->
      headers["Content-Type"] = encoding
    }

    setBody(body)
  }
}

internal suspend inline fun <reified T : Any> HttpResponse.toAtpResponse(): AtpResponse<T> {
  val headers = headers.entries().associateByTo(mutableMapOf(), { it.key }, { it.value.last() })

  return when (val code = StatusCode.fromCode(status.value)) {
    is StatusCode.Okay -> {
      AtpResponse.Success(
        headers = headers,
        response = body(),
      )
    }
    is StatusCode.Failure -> {
      val maybeBody = runCatching<T> { body() }.getOrNull()
      val maybeError = if (maybeBody == null) {
        runCatching<AtpErrorDescription> { body() }.getOrNull()
      } else {
        null
      }

      return AtpResponse.Failure(
        headers = headers,
        statusCode = code,
        response = maybeBody,
        error = maybeError,
      )
    }
  }
}
