package sh.christian.ozone.api.xrpc

import app.bsky.actor.GetProfileQueryParams
import app.bsky.actor.GetProfileResponse
import app.bsky.feed.GetTimelineQueryParams
import app.bsky.feed.GetTimelineResponse
import com.atproto.session.CreateRequest
import com.atproto.session.CreateResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import sh.christian.ozone.api.AtpApi
import sh.christian.ozone.api.Encoding
import sh.christian.ozone.api.response.AtpErrorDescription
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.api.response.StatusCode

class XrpcApi(
  private val host: StateFlow<String>,
  private val tokens: MutableStateFlow<Tokens?>,
) : AtpApi {
  private val json = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "${'$'}type"
  }

  private val client = HttpClient(CIO) {
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.INFO
    }

    install(ContentNegotiation) {
      json(json)
    }

    XrpcAuth(json, tokens)

    expectSuccess = false

    defaultRequest {
      val hostUrl = Url(this@XrpcApi.host.value)
      url.protocol = hostUrl.protocol
      url.host = hostUrl.host
      url.port = hostUrl.port
    }
  }

  override suspend fun createSession(
    request: CreateRequest,
  ): AtpResponse<CreateResponse> {
    return client.procedure("/xrpc/com.atproto.session.create", request).toAtpResponse()
  }

  override suspend fun getTimeline(
    params: GetTimelineQueryParams,
  ): AtpResponse<GetTimelineResponse> {
    return client.query("/xrpc/app.bsky.feed.getTimeline", params.toMap()).toAtpResponse()
  }

  override suspend fun getProfile(
    params: GetProfileQueryParams,
  ): AtpResponse<GetProfileResponse> {
    return client.query("/xrpc/app.bsky.actor.getProfile", params.toMap()).toAtpResponse()
  }

  private suspend inline fun HttpClient.query(
    path: String,
    queryParams: Map<String, Any?>,
  ): HttpResponse {
    return get(path) {
      queryParams.forEach(::parameter)
    }
  }

  private suspend inline fun <reified T : Any> HttpClient.procedure(
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
