package sh.christian.ozone.api.xrpc

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
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import sh.christian.ozone.api.AtpApi
import sh.christian.ozone.api.Encoding
import sh.christian.ozone.api.response.AtpErrorDescription
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.api.response.StatusCode

class XrpcApi(host: String) : AtpApi {
  private val hostUrl = Url(host)
  private val client = HttpClient(CIO) {
    install(ContentNegotiation) {
      json(
        json = Json {
          classDiscriminator = "${'$'}type"
        }
      )
    }

    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.BODY
    }

    expectSuccess = false

    defaultRequest {
      url.protocol = hostUrl.protocol
      url.host = hostUrl.host
      url.port = hostUrl.port

      headers["Content-Type"] = "application/json"
      authenticationData?.let { auth ->
        headers["Authorization"] = auth
      }
    }
  }

  private var authenticationData: String? = null

  override fun setAuthentication(authenticationData: String?) {
    this.authenticationData = authenticationData
  }

  override suspend fun createSession(request: CreateRequest): AtpResponse<CreateResponse> {
    return client.procedure("/xrpc/com.atproto.session.create", request).toAtpResponse()
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

  private suspend inline fun <reified T : Any> HttpResponse.toAtpResponse(): AtpResponse<T> {
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
}
