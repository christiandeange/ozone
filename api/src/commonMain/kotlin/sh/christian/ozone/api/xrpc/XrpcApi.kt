package sh.christian.ozone.api.xrpc

import app.bsky.actor.GetProfileQueryParams
import app.bsky.actor.GetProfileResponse
import app.bsky.feed.GetAuthorFeedQueryParams
import app.bsky.feed.GetAuthorFeedResponse
import app.bsky.feed.GetTimelineQueryParams
import app.bsky.feed.GetTimelineResponse
import com.atproto.repo.CreateRecordRequest
import com.atproto.repo.CreateRecordResponse
import com.atproto.session.CreateRequest
import com.atproto.session.CreateResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import sh.christian.ozone.api.AtpApi
import sh.christian.ozone.api.response.AtpResponse

class XrpcApi(
  private val host: StateFlow<String>,
  private val tokens: MutableStateFlow<Tokens?>,
) : AtpApi {
  private val jsonEnvironment = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "${'$'}type"
  }

  private val client = HttpClient(CIO) {
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.INFO
    }

    install(ContentNegotiation) {
      json(jsonEnvironment)
    }

    install(XrpcAuthPlugin) {
      json = jsonEnvironment
      authTokens = tokens
    }

    install(DefaultRequest) {
      val hostUrl = Url(this@XrpcApi.host.value)
      url.protocol = hostUrl.protocol
      url.host = hostUrl.host
      url.port = hostUrl.port
    }

    expectSuccess = false
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

  override suspend fun createRecord(
    request: CreateRecordRequest,
  ): AtpResponse<CreateRecordResponse> {
    return client.procedure("/xrpc/com.atproto.repo.createRecord", request).toAtpResponse()
  }

  override suspend fun getAuthorFeed(
    params: GetAuthorFeedQueryParams,
  ): AtpResponse<GetAuthorFeedResponse> {
    return client.query("/xrpc/app.bsky.feed.getAuthorFeed", params.toMap()).toAtpResponse()
  }
}
