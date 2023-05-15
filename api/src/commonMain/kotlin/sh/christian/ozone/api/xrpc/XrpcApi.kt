package sh.christian.ozone.api.xrpc

import app.bsky.actor.GetProfileQueryParams
import app.bsky.actor.GetProfileResponse
import app.bsky.feed.GetAuthorFeedQueryParams
import app.bsky.feed.GetAuthorFeedResponse
import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponse
import app.bsky.feed.GetTimelineQueryParams
import app.bsky.feed.GetTimelineResponse
import app.bsky.notification.ListNotificationsQueryParams
import app.bsky.notification.ListNotificationsResponse
import com.atproto.repo.CreateRecordRequest
import com.atproto.repo.CreateRecordResponse
import com.atproto.server.CreateAccountRequest
import com.atproto.server.CreateAccountResponse
import com.atproto.server.CreateSessionRequest
import com.atproto.server.CreateSessionResponse
import com.atproto.server.DescribeServerResponse
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
    request: CreateSessionRequest,
  ): AtpResponse<CreateSessionResponse> {
    return client.procedure("/xrpc/com.atproto.server.createSession", request).toAtpResponse()
  }

  override suspend fun createAccount(
    request: CreateAccountRequest,
  ): AtpResponse<CreateAccountResponse> {
    return client.procedure("/xrpc/com.atproto.server.createAccount", request).toAtpResponse()
  }

  override suspend fun getTimeline(
    params: GetTimelineQueryParams,
  ): AtpResponse<GetTimelineResponse> {
    return client.query("/xrpc/app.bsky.feed.getTimeline", params.asList()).toAtpResponse()
  }

  override suspend fun getProfile(
    params: GetProfileQueryParams,
  ): AtpResponse<GetProfileResponse> {
    return client.query("/xrpc/app.bsky.actor.getProfile", params.asList()).toAtpResponse()
  }

  override suspend fun createRecord(
    request: CreateRecordRequest,
  ): AtpResponse<CreateRecordResponse> {
    return client.procedure("/xrpc/com.atproto.repo.createRecord", request).toAtpResponse()
  }

  override suspend fun getAuthorFeed(
    params: GetAuthorFeedQueryParams,
  ): AtpResponse<GetAuthorFeedResponse> {
    return client.query("/xrpc/app.bsky.feed.getAuthorFeed", params.asList()).toAtpResponse()
  }

  override suspend fun getPostThread(
    params: GetPostThreadQueryParams,
  ): AtpResponse<GetPostThreadResponse> {
    return client.query("/xrpc/app.bsky.feed.getPostThread", params.asList()).toAtpResponse()
  }

  override suspend fun listNotifications(
    params: ListNotificationsQueryParams,
  ): AtpResponse<ListNotificationsResponse> {
    return client.query("/xrpc/app.bsky.notification.listNotifications", params.asList())
      .toAtpResponse()
  }

  override suspend fun describeServer(): AtpResponse<DescribeServerResponse> {
    return client.query("/xrpc/com.atproto.server.describeServer").toAtpResponse()
  }
}
