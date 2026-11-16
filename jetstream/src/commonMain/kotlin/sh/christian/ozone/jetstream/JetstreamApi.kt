package sh.christian.ozone.jetstream

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import network.bsky.jetstream.GetBlockQueryParams
import network.bsky.jetstream.GetSegmentQueryParams
import network.bsky.jetstream.ListSegmentsQueryParams
import network.bsky.jetstream.ListSegmentsResponse
import network.bsky.jetstream.PlanSnapshotRequest
import network.bsky.jetstream.PlanSnapshotResponse
import network.bsky.jetstream.SubscribeMessage
import network.bsky.jetstream.SubscribeOptionsUpdate
import network.bsky.jetstream.SubscribeQueryParams
import network.bsky.jetstream.SubscribeSourcedMessage
import sh.christian.ozone.BlueskyJson
import sh.christian.ozone.api.model.JsonContent.Companion.encodeAsJsonContent
import sh.christian.ozone.api.xrpc.XrpcSerializersModule
import sh.christian.ozone.api.xrpc.defaultHttpClient
import sh.christian.ozone.api.xrpc.toAtpModel
import sh.christian.ozone.api.xrpc.withXrpcConfiguration

/** Implementation of the Jetstream v2 streaming and archive APIs. */
class JetstreamApi(httpClient: HttpClient) : JetstreamInterface {

  /** Construct a new instance using a free-form [hostName]. */
  constructor(hostName: String) : this(
    defaultHttpClient.config {
      install(DefaultRequest) {
        url.host = hostName
      }
    }
  )

  /** Construct a new instance using a defined [host]. */
  constructor(host: JetstreamHost) : this("jetstream.${host.region}.bsky.network")

  /** Construct a new instance using the [US_EAST][JetstreamHost.US_EAST] region. */
  constructor() : this(JetstreamHost.US_EAST)

  private val client: HttpClient = httpClient.withXrpcConfiguration(XrpcSerializersModule)

  override suspend fun subscribe(params: SubscribeQueryParams): Flow<SubscribeMessage> = flow {
    withSubscribe(params) {
      emitAll(incoming.messages())
    }
  }

  override suspend fun subscribe(
    params: SubscribeQueryParams,
    block: suspend SubscriptionContext.() -> Unit,
  ) {
    withSubscribe(params) {
      val incomingMessages = incoming.messages()
      val outgoingMessages = MutableSharedFlow<SubscribeSourcedMessage>()

      val outgoingJob = launch(start = CoroutineStart.UNDISPATCHED) {
        outgoingMessages.collect { sourcedMessage ->
          outgoing.send(Frame.Text(BlueskyJson.encodeToString(sourcedMessage)))
        }
      }

      try {
        DefaultSubscriptionContext(outgoingMessages, incomingMessages).block()
      } finally {
        outgoingJob.cancelAndJoin()
      }
    }
  }

  override suspend fun listSegments(params: ListSegmentsQueryParams): ListSegmentsResponse =
    client.get("/xrpc/network.bsky.jetstream.listSegments") {
      params.asList().forEach { (key, value) -> parameter(key, value) }
    }.toAtpModel()

  override suspend fun planSnapshot(request: PlanSnapshotRequest): PlanSnapshotResponse =
    client.post("/xrpc/network.bsky.jetstream.planSnapshot") {
      headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      setBody(BlueskyJson.encodeToString(request))
    }.toAtpModel()

  override suspend fun getSegment(params: GetSegmentQueryParams): ByteArray =
    client.get("/xrpc/network.bsky.jetstream.getSegment") {
      params.asList().forEach { (key, value) -> parameter(key, value) }
    }.toAtpModel<ByteArray>()

  override suspend fun getBlock(params: GetBlockQueryParams): ByteArray =
    client.get("/xrpc/network.bsky.jetstream.getBlock") {
      params.asList().forEach { (key, value) -> parameter(key, value) }
    }.toAtpModel<ByteArray>()

  private suspend fun withSubscribe(
    params: SubscribeQueryParams,
    block: suspend DefaultClientWebSocketSession.() -> Unit,
  ) {
    client.wss(
      path = "/subscribe",
      request = { params.asList().forEach { (key, value) -> parameter(key, value) } },
    ) {
      initZstd()
      block()
    }
  }

  private fun ReceiveChannel<Frame>.messages(): Flow<SubscribeMessage> {
    return receiveAsFlow()
      .mapNotNull { frame ->
        when (frame) {
          // zstd-compressed json
          is Frame.Binary -> decompressZstd(frame.data)
          // raw json
          is Frame.Text -> frame.data
          // ignored
          is Frame.Close,
          is Frame.Ping,
          is Frame.Pong -> null
          else -> null
        }
      }
      .map { data ->
        BlueskyJson.decodeFromString(
          deserializer = SubscribeMessage.serializer(),
          string = data.decodeToString(),
        )
      }
  }

  private class DefaultSubscriptionContext(
    private val sourcedMessages: MutableSharedFlow<SubscribeSourcedMessage>,
    override val messages: Flow<SubscribeMessage>,
  ) : SubscriptionContext {
    override suspend fun updateSubscription(message: SubscribeOptionsUpdate) {
      sourcedMessages.emit(
        SubscribeSourcedMessage(
          type = "options_update",
          payload = BlueskyJson.encodeAsJsonContent(message),
        )
      )
    }
  }
}
