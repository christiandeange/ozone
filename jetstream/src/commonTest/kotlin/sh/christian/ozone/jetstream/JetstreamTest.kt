package sh.christian.ozone.jetstream

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.InternalAPI
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketExtension
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import network.bsky.jetstream.GetBlockQueryParams
import network.bsky.jetstream.GetSegmentQueryParams
import network.bsky.jetstream.ListSegmentsQueryParams
import network.bsky.jetstream.PlanSnapshotRequest
import network.bsky.jetstream.SubscribeMessage
import network.bsky.jetstream.SubscribeOptionsUpdate
import network.bsky.jetstream.SubscribeQueryParams
import sh.christian.ozone.BlueskyJson
import sh.christian.ozone.api.Did
import sh.christian.ozone.api.response.AtpException
import kotlin.coroutines.CoroutineContext
import kotlin.io.encoding.Base64
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

class JetstreamTest {
  private val event = """
    {
        "did": "did:plc:test",
        "time_us": 1794787200000000,
        "cursor": 4294967297,
        "kind": "sync",
        "sync": {
            "rev": "test",
            "futureField": true
        }
    }
    """.trimIndent()

  @BeforeTest
  fun setUp() = prepareJetstreamTestRuntime()

  @Test
  fun listSegmentsUsesPaginationAndDecodesLargeSequences() = runTest {
    val engine = MockEngine {
      respond(
        """
        {
            "cursor": "next",
            "segments": [
                {
                    "name": "seg_000000002a.jss",
                    "index": 42,
                    "sizeBytes": 123,
                    "checksum": "0123456789abcdef",
                    "eventCount": 10,
                    "minSeq": 4294967297,
                    "maxSeq": 4294967306,
                    "minWitnessedAt": 1794787200000000,
                    "maxWitnessedAt": 1794787200000010
                }
            ]
        }  
        """.trimIndent(),
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
      )
    }
    HttpClient(engine).use { client ->
      val result = JetstreamApi(client).listSegments(
        ListSegmentsQueryParams(
          limit = 2,
          cursor = "opaque+/="
        )
      )
      assertEquals("next", result.cursor)
      assertEquals(4294967297L, result.segments.single().minSeq)
      assertEquals(1794787200000010L, result.segments.single().maxWitnessedAt)
    }
  }

  @Test
  fun planSnapshotPostsFiltersAndDecodesBlockRanges() = runTest {
    val engine = MockEngine {
      respond(
        """
        {
            "plannedThroughSeq": 4294967397,
            "sealedTipSeq": 4294967397,
            "segments": [
                {
                    "name": "seg_000000002a.jss",
                    "index": 42,
                    "checksum": "0123456789abcdef",
                    "minSeq": 4294967298,
                    "maxSeq": 4294967397,
                    "mode": "blocks",
                    "blocks": [
                        {
                            "first": 0,
                            "last": 2
                        }
                    ]
                }
            ],
            "stats": {
                "segmentsExamined": 1,
                "segmentsMatched": 1,
                "blocksMatched": 3,
                "entries": 1
            }
        }
        """.trimIndent(),
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
      )
    }
    HttpClient(engine).use { client ->
      val result = JetstreamApi(client).planSnapshot(
        PlanSnapshotRequest(
          dids = listOf(Did("did:plc:test")),
          collections = listOf("app.bsky.feed.*"),
          afterSeq = 4294967297L,
          beforeSeq = 4294967397L,
        )
      )
      assertEquals(result.sealedTipSeq, result.plannedThroughSeq)
      assertEquals(2L, result.segments.single().blocks!!.single().last)
      assertEquals(3L, result.stats.blocksMatched)
    }
  }

  @Test
  fun jsonEndpointsReportHttpErrorsWithDescriptions() = runTest {
    for (status in listOf(
      HttpStatusCode.BadRequest,
      HttpStatusCode.Unauthorized,
      HttpStatusCode.InternalServerError,
    )) {
      val engine = MockEngine {
        respond(
          """
          {
              "error": "RequestFailed",
              "message": "Test failure"
          }
          """.trimIndent(),
          status = status,
          headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
      }
      HttpClient(engine).use { client ->
        val api = JetstreamApi(client)
        val listError = assertFailsWith<AtpException> { api.listSegments() }
        assertEquals(status.value, listError.statusCode.code)
        assertEquals("RequestFailed", listError.error?.error)
        assertEquals("Test failure", listError.error?.message)
        val planError = assertFailsWith<AtpException> { api.planSnapshot() }
        assertEquals(status.value, planError.statusCode.code)
        assertEquals("RequestFailed", planError.error?.error)
        assertEquals("Test failure", planError.error?.message)
      }
    }
  }

  @Test
  fun archiveDownloadsPreserveBinaryBytes() = runTest {
    val bytes = byteArrayOf(0, 1, 0x80.toByte(), 0xff.toByte(), 10)
    val engine = MockEngine {
      respond(bytes, headers = headersOf(HttpHeaders.ContentType, "application/octet-stream"))
    }
    HttpClient(engine).use { client ->
      val api = JetstreamApi(client)
      assertContentEquals(bytes, api.getSegment(GetSegmentQueryParams(name = "seg_000000002a.jss")))
      assertContentEquals(
        bytes,
        api.getBlock(
          GetBlockQueryParams(
            segment = "seg_000000002a.jss",
            blockIndex = 2
          )
        )
      )
    }
  }

  @Test
  fun archiveDownloadRejectsHttpErrors() = runTest {
    for (status in listOf(
      HttpStatusCode.NotFound,
      HttpStatusCode.Unauthorized,
      HttpStatusCode.InternalServerError
    )) {
      val engine = MockEngine {
        respond(
          """
          {
              "error": "DownloadFailed"
          }
          """.trimIndent(),
          status = status,
          headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
      }
      HttpClient(engine).use { client ->
        val api = JetstreamApi(client)
        val segmentError = assertFailsWith<AtpException> {
          api.getSegment(GetSegmentQueryParams(name = "missing.jss"))
        }
        assertEquals(status.value, segmentError.statusCode.code)
        assertEquals("DownloadFailed", segmentError.error?.error)
        val blockError = assertFailsWith<AtpException> {
          api.getBlock(GetBlockQueryParams(segment = "missing.jss", blockIndex = 0))
        }
        assertEquals(status.value, blockError.statusCode.code)
        assertEquals("DownloadFailed", blockError.error?.error)
      }
    }
  }

  @Test
  fun streamDecodesTextAndSendsRepeatedFilters() = runTest {
    val socket = TestSocket(coroutineContext)
    socket.incoming.send(Frame.Text(event))
    val engine = websocketEngine(socket)
    HttpClient(engine).use { client ->
      val result = JetstreamApi(client).subscribe(
        SubscribeQueryParams(
          wantedCollections = listOf("app.bsky.feed.post", "app.bsky.feed.*"),
          wantedDids = listOf(Did("did:plc:one"), Did("did:plc:two")),
          cursor = 4294967297L,
        )
      ).first()
      assertEquals(4294967297L, result.cursor)
      assertIs<SubscribeEventContent.Sync>(result.content)
    }
  }

  @Test
  fun streamDecodesLegacyZstdFrames() = runTest {
    // The event above, compressed with the bundled Jetstream dictionary at level 3.
    val compressed = Base64.decode(
      "KLUv/SNtRhVgfGUCAAPEDJPJ5HmvsSGfWW8wM998ScFGvF9vrLEhb3U2UvA+1HMf7EOxNTYy1pnHjRR0qOfx2IeeOw" +
          "b8lVU8pTfOpn4tsba4LGL4qYeoCwE=",
    )
    val socket = TestSocket(coroutineContext)
    socket.incoming.send(Frame.Binary(true, compressed))
    HttpClient(websocketEngine(socket)).use { client ->
      val result = JetstreamApi(client).subscribe(SubscribeQueryParams(compress = true)).first()
      assertEquals(BlueskyJson.decodeFromString<SubscribeMessage>(event), result)
      assertIs<SubscribeEventContent.Sync>(result.content)
    }
  }

  @Test
  fun requireHelloDeliversImmediateOptionsUpdate() = runTest {
    val socket = TestSocket(coroutineContext)
    HttpClient(websocketEngine(socket)).use { client ->
      JetstreamApi(client).subscribe(SubscribeQueryParams(requireHello = true)) {
        // This must work before collecting messages: the server is waiting for this hello.
        updateSubscription(SubscribeOptionsUpdate(wantedCollections = listOf("app.bsky.feed.post")))
        val frame = withTimeoutOrNull(1.seconds) { socket.outgoing.receive() }
        assertNotNull(
          frame,
          "The first options_update was dropped; requireHello leaves the server waiting forever"
        )
        val envelope = BlueskyJson.parseToJsonElement((frame as Frame.Text).readText()).jsonObject
        assertEquals(JsonPrimitive("options_update"), envelope["type"])
        assertEquals(
          JsonArray(listOf(JsonPrimitive("app.bsky.feed.post"))),
          envelope["payload"]!!.jsonObject["wantedCollections"]
        )
      }
    }
  }

  @Test
  fun optionsUpdateIsDeliveredOnceSenderHasStarted() = runTest {
    val socket = TestSocket(coroutineContext)
    HttpClient(websocketEngine(socket)).use { client ->
      JetstreamApi(client).subscribe(SubscribeQueryParams(requireHello = true)) {
        yield()
        updateSubscription(SubscribeOptionsUpdate(wantedCollections = listOf("app.bsky.feed.post")))
        val frame = withTimeout(1.seconds) { socket.outgoing.receive() }
        val envelope = BlueskyJson.parseToJsonElement((frame as Frame.Text).readText()).jsonObject
        assertEquals(JsonPrimitive("options_update"), envelope["type"])
        assertEquals(
          JsonArray(listOf(JsonPrimitive("app.bsky.feed.post"))),
          envelope["payload"]!!.jsonObject["wantedCollections"]
        )
      }
    }
  }

  @Test
  fun malformedStreamEventIsReportedToCollector() = runTest {
    val socket = TestSocket(coroutineContext)
    socket.incoming.send(Frame.Text("""{"kind":"sync"}"""))
    socket.incoming.close()
    HttpClient(websocketEngine(socket)).use { client ->
      assertFailsWith<SerializationException> {
        JetstreamApi(client).subscribe(SubscribeQueryParams()).toList()
      }
    }
  }

  @Test
  fun commitEventExposesContent() {
    assertEventContent<SubscribeEventContent.Commit>(
      kind = "commit",
      payload = """
        {
            "rev": "rev",
            "operation": "delete",
            "collection": "app.bsky.feed.post",
            "rkey": "key"
        }
      """.trimIndent(),
    )
  }

  @Test
  fun identityEventExposesContent() {
    assertEventContent<SubscribeEventContent.Identity>(
      kind = "identity",
      payload = """
        {
            "seq": 1,
            "did": "did:plc:test",
            "time": "2026-11-15T00:00:00Z",
            "handle": "test.bsky.social"
        }
      """.trimIndent(),
    )
  }

  @Test
  fun accountEventExposesContent() {
    assertEventContent<SubscribeEventContent.Account>(
      kind = "account",
      payload = """
        {
            "seq": 1,
            "did": "did:plc:test",
            "time": "2026-11-15T00:00:00Z",
            "active": false,
            "status": "deactivated"
        }
      """.trimIndent(),
    )
  }

  @Test
  fun syncEventExposesContent() {
    assertEventContent<SubscribeEventContent.Sync>(
      kind = "sync",
      payload = """
        {
            "rev": "rev"
        }
      """.trimIndent(),
    )
  }

  private inline fun <reified T : SubscribeEventContent> assertEventContent(
    kind: String,
    payload: String,
  ) {
    val decoded = BlueskyJson.decodeFromString<SubscribeMessage>(
      """
        {
            "did": "did:plc:test",
            "time_us": 1794787200000000,
            "cursor": 4294967297,
            "kind": "$kind",
            "$kind": $payload
        }
      """.trimIndent(),
    )
    assertIs<T>(decoded.content)
  }

  private fun websocketEngine(socket: TestSocket): MockEngine {
    return MockEngine {
      HttpResponseData(
        statusCode = HttpStatusCode.SwitchingProtocols,
        requestTime = GMTDate(),
        headers = Headers.Empty,
        version = HttpProtocolVersion.HTTP_1_1,
        body = socket,
        callContext = currentCoroutineContext() + Job(),
      )
    }
  }

  private class TestSocket(
    override val coroutineContext: CoroutineContext,
  ) : DefaultWebSocketSession {
    override var masking = false
    override var maxFrameSize = Long.MAX_VALUE
    override var pingIntervalMillis = 0L
    override var timeoutMillis = 15000L
    override val incoming = Channel<Frame>(Channel.UNLIMITED)
    override val outgoing = Channel<Frame>(Channel.UNLIMITED)
    override val extensions = emptyList<WebSocketExtension<*>>()
    override val closeReason = CompletableDeferred<CloseReason?>()

    @OptIn(InternalAPI::class)
    override fun start(negotiatedExtensions: List<WebSocketExtension<*>>) = Unit

    override suspend fun flush() = Unit

    @Deprecated("Required by WebSocketSession")
    override fun terminate() = Unit
  }
}
