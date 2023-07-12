package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import sh.christian.ozone.api.response.AtpErrorDescription
import sh.christian.ozone.api.response.AtpException
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.api.response.StatusCode

suspend inline fun HttpClient.query(
  path: String,
  queryParams: List<Pair<String, Any?>> = emptyList(),
): HttpResponse {
  return get(path) {
    queryParams.forEach { (key, value) -> parameter(key, value) }
  }
}

suspend inline fun HttpClient.procedure(path: String): HttpResponse {
  return post(path)
}

suspend inline fun <reified T : Any> HttpClient.procedure(
  path: String,
  body: T,
  encoding: String,
): HttpResponse {
  return post(path) {
    headers["Content-Type"] = encoding
    setBody(body)
  }
}

suspend inline fun <reified T : Any> HttpResponse.toAtpModel(): T {
  return when (val status = StatusCode.fromCode(status.value)) {
    is StatusCode.Okay -> body<T>()
    is StatusCode.Failure -> throw AtpException(status)
  }
}

suspend inline fun <reified T : Any> HttpResponse.toAtpResult(): Result<T> {
  return when (val status = StatusCode.fromCode(status.value)) {
    is StatusCode.Okay -> Result.success(body<T>())
    is StatusCode.Failure -> Result.failure(AtpException(status))
  }
}

suspend inline fun <reified T : Any> HttpResponse.toAtpResponse(): AtpResponse<T> {
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
