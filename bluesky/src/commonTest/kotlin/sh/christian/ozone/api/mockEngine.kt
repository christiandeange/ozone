package sh.christian.ozone.api

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.Serializable
import sh.christian.ozone.BlueskyJson
import sh.christian.ozone.api.response.AtpErrorDescription

inline fun <reified T : @Serializable Any> mockEngine(
  response: T,
  statusCode: HttpStatusCode = HttpStatusCode.OK,
): MockEngine {
  return mockEngine(statusCode = statusCode) { BlueskyJson.encodeToString(response) }
}

inline fun mockEngine(
  error: AtpErrorDescription,
  statusCode: HttpStatusCode = HttpStatusCode.BadRequest,
): MockEngine {
  return mockEngine(response = error, statusCode = statusCode)
}

inline fun mockEngine(
  statusCode: HttpStatusCode = HttpStatusCode.OK,
  noinline responseProvider: (HttpRequestData) -> String,
): MockEngine {
  return MockEngine { request ->
    respond(
      content = ByteReadChannel(responseProvider(request)),
      status = statusCode,
      headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )
  }
}
