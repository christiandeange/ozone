package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.ExperimentalSerializationApi
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

suspend fun HttpClient.subscription(
  path: String,
  queryParams: List<Pair<String, Any?>> = emptyList(),
): Flow<XrpcSubscriptionResponse> = flow {
  wss(
    path = path,
    request = { queryParams.forEach { (key, value) -> parameter(key, value) } },
  ) {
    emitAll(
      incoming.receiveAsFlow()
        .filterIsInstance<Frame.Binary>()
        .map { frame -> XrpcSubscriptionResponse(frame.data) }
    )
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

inline fun <reified T : Any> Flow<XrpcSubscriptionResponse>.toAtpModel(
  noinline subscriptionSerializerProvider: SubscriptionSerializerProvider<T>,
): Flow<T> {
  return toAtpResult<T>(subscriptionSerializerProvider).map { it.getOrThrow() }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Any> Flow<XrpcSubscriptionResponse>.toAtpResult(
  noinline subscriptionSerializerProvider: SubscriptionSerializerProvider<T>,
): Flow<Result<T>> {
  return map { response -> runCatching { response.body(subscriptionSerializerProvider) } }
}

inline fun <reified T : Any> Flow<XrpcSubscriptionResponse>.toAtpResponse(
  noinline subscriptionSerializerProvider: SubscriptionSerializerProvider<T>,
): Flow<AtpResponse<T>> {
  return toAtpResult<T>(subscriptionSerializerProvider).map {
    it.fold(
      onSuccess = { body ->
        AtpResponse.Success(
          response = body,
          headers = emptyMap(),
        )
      },
      onFailure = { e ->
        AtpResponse.Failure(
          statusCode = StatusCode.InvalidRequest,
          response = null,
          error = (e as? XrpcSubscriptionParseException)?.error,
          headers = emptyMap(),
        )
      }
    )
  }
}
