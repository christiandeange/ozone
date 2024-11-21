package sh.christian.ozone.jetstream

import app.bsky.jetstream.SubscribeMessage
import app.bsky.jetstream.SubscribeOptionsUpdate
import app.bsky.jetstream.SubscribeQueryParams
import app.bsky.jetstream.SubscribeSourcedMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.parameter
import io.ktor.websocket.Frame
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import sh.christian.ozone.BlueskyJson
import sh.christian.ozone.api.model.JsonContent.Companion.encodeAsJsonContent
import sh.christian.ozone.api.xrpc.defaultHttpClient

/**
 * Implementation to interact with a hosted Jetstream service.
 *
 * @constructor Construct a new instance using an existing [HttpClient].
 */
class JetstreamApi(httpClient: HttpClient) : JetstreamInterface {

  /** Construct a new instance using a free-form [hostName]. */
  constructor(hostName: String) : this(
    defaultHttpClient.config {
      install(DefaultRequest) {
        url.host = hostName
      }
    }
  )

  /** Construct a new instance using a well-known [JetstreamHost] instance. */
  constructor(host: JetstreamHost) : this("jetstream${host.instance}.${host.region}.bsky.network")

  /** Construct a new instance that connects to the [JetstreamHost.JETSTREAM_1_US_EAST] instance. */
  constructor() : this(JetstreamHost.JETSTREAM_1_US_EAST)

  private val client: HttpClient = httpClient.config {
    install(WebSockets)
  }

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

      val outgoingJob = launch {
        outgoingMessages.collect { sourcedMessage ->
          outgoing.send(Frame.Text(BlueskyJson.encodeToString(sourcedMessage)))
        }
      }

      try {
        DefaultSubscriptionContext(outgoingMessages, incomingMessages).apply {
          block()
        }
      } finally {
        outgoingJob.cancelAndJoin()
      }
    }
  }

  private suspend fun withSubscribe(
    params: SubscribeQueryParams,
    block: suspend DefaultClientWebSocketSession.() -> Unit
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
      .catch { it.printStackTrace() }
  }

  private inner class DefaultSubscriptionContext(
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
