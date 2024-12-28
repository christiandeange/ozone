package sh.christian.ozone.api

import com.atproto.server.CreateAccountRequest
import com.atproto.server.CreateAccountResponse
import com.atproto.server.CreateSessionRequest
import com.atproto.server.CreateSessionResponse
import com.atproto.server.RefreshSessionResponse
import io.ktor.client.HttpClient
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import sh.christian.ozone.BlueskyJson
import sh.christian.ozone.XrpcBlueskyApi
import sh.christian.ozone.api.AuthenticatedXrpcBlueskyApi.Companion.authenticated
import sh.christian.ozone.api.response.AtpErrorDescription
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthenticatedXrpcBlueskyApiTest {

  @Test
  fun testInitialTokensFromConstructor() = runTest {
    val tokens = BlueskyAuthPlugin.Tokens("accessJwt", "refreshJwt")
    val api = AuthenticatedXrpcBlueskyApi(HttpClient(mockEngine("")), tokens)
    assertEquals(tokens, api.authTokens.value)
  }

  @Test
  fun testInitialTokensFromFactory() = runTest {
    val tokens = BlueskyAuthPlugin.Tokens("accessJwt", "refreshJwt")
    val api = XrpcBlueskyApi(HttpClient(mockEngine(""))).authenticated(tokens)
    assertEquals(tokens, api.authTokens.value)
  }

  @Test
  fun testCreateAccount() = runTest {
    val request = CreateAccountRequest(
      email = "bob@gmail.com",
      handle = Handle("bob.bsky.social"),
      password = "password",
    )
    val response = CreateAccountResponse(
      accessJwt = "accessJwt",
      refreshJwt = "refreshJwt",
      handle = Handle("bob.bsky.social"),
      did = Did("did:plc:123"),
    )

    val api = AuthenticatedXrpcBlueskyApi(HttpClient(mockEngine(response)))
    assertNull(api.authTokens.value)
    api.createAccount(request)
    assertEquals(BlueskyAuthPlugin.Tokens("accessJwt", "refreshJwt"), api.authTokens.value)
  }

  @Test
  fun testCreateSession() = runTest {
    val request = CreateSessionRequest(
      identifier = "bob.bsky.social",
      password = "password",
    )
    val response = CreateSessionResponse(
      accessJwt = "accessJwt",
      refreshJwt = "refreshJwt",
      handle = Handle("bob.bsky.social"),
      did = Did("did:plc:123"),
    )

    val api = AuthenticatedXrpcBlueskyApi(HttpClient(mockEngine(response)))
    assertNull(api.authTokens.value)
    api.createSession(request)
    assertEquals(BlueskyAuthPlugin.Tokens("accessJwt", "refreshJwt"), api.authTokens.value)
  }

  @Test
  fun testRefreshSession() = runTest {
    val response = RefreshSessionResponse(
      accessJwt = "accessJwt",
      refreshJwt = "refreshJwt",
      handle = Handle("bob.bsky.social"),
      did = Did("did:plc:123"),
    )

    val api = AuthenticatedXrpcBlueskyApi(HttpClient(mockEngine(response)))
    assertNull(api.authTokens.value)
    api.refreshSession()
    assertEquals(BlueskyAuthPlugin.Tokens("accessJwt", "refreshJwt"), api.authTokens.value)
  }

  @Test
  fun testCreateThenRefreshSession() = runTest {
    val createRequest = CreateSessionRequest(
      identifier = "bob.bsky.social",
      password = "password",
    )
    val createResponse = CreateSessionResponse(
      accessJwt = "accessJwt-1",
      refreshJwt = "refreshJwt-1",
      handle = Handle("bob.bsky.social"),
      did = Did("did:plc:123"),
    )
    val refreshResponse = RefreshSessionResponse(
      accessJwt = "accessJwt-2",
      refreshJwt = "refreshJwt-2",
      handle = Handle("bob.bsky.social"),
      did = Did("did:plc:123"),
    )

    val api = AuthenticatedXrpcBlueskyApi(HttpClient(mockEngine { request ->
      when (request.url.encodedPath) {
        "/xrpc/com.atproto.server.createSession" -> BlueskyJson.encodeToString(createResponse)
        "/xrpc/com.atproto.server.refreshSession" -> BlueskyJson.encodeToString(refreshResponse)
        else -> error("Unexpected request: ${request.url}")
      }
    }))

    assertNull(api.authTokens.value)
    api.createSession(createRequest)
    assertEquals(BlueskyAuthPlugin.Tokens("accessJwt-1", "refreshJwt-1"), api.authTokens.value)
    api.refreshSession()
    assertEquals(BlueskyAuthPlugin.Tokens("accessJwt-2", "refreshJwt-2"), api.authTokens.value)
  }

  @Test
  fun testDeleteSession() = runTest {
    val createRequest = CreateSessionRequest(
      identifier = "bob.bsky.social",
      password = "password",
    )
    val createResponse = CreateSessionResponse(
      accessJwt = "accessJwt",
      refreshJwt = "refreshJwt",
      handle = Handle("bob.bsky.social"),
      did = Did("did:plc:123"),
    )

    val api = AuthenticatedXrpcBlueskyApi(HttpClient(mockEngine { request ->
      when (request.url.encodedPath) {
        "/xrpc/com.atproto.server.createSession" -> BlueskyJson.encodeToString(createResponse)
        "/xrpc/com.atproto.server.deleteSession" -> ""
        else -> error("Unexpected request: ${request.url}")
      }
    }))

    assertNull(api.authTokens.value)
    api.createSession(createRequest)
    assertEquals(BlueskyAuthPlugin.Tokens("accessJwt", "refreshJwt"), api.authTokens.value)
    api.deleteSession()
    assertNull(api.authTokens.value)
  }

  @Test
  fun testClearCredentials() = runTest {
    val createRequest = CreateSessionRequest(
      identifier = "bob.bsky.social",
      password = "password",
    )
    val createResponse = CreateSessionResponse(
      accessJwt = "accessJwt",
      refreshJwt = "refreshJwt",
      handle = Handle("bob.bsky.social"),
      did = Did("did:plc:123"),
    )

    val api = AuthenticatedXrpcBlueskyApi(HttpClient(mockEngine(createResponse)))

    assertNull(api.authTokens.value)
    api.createSession(createRequest)
    assertEquals(BlueskyAuthPlugin.Tokens("accessJwt", "refreshJwt"), api.authTokens.value)
    api.clearCredentials()
    assertNull(api.authTokens.value)
  }

  @Test
  fun testFailingNetworkCallDoesNotSave() = runTest {
    val createRequest = CreateSessionRequest(
      identifier = "bob.bsky.social",
      password = "password",
    )
    val error = AtpErrorDescription(
      error = "AuthFactorTokenRequired",
      message = "Needs 2FA",
    )

    val api = AuthenticatedXrpcBlueskyApi(HttpClient(mockEngine(error)))

    assertNull(api.authTokens.value)
    api.createSession(createRequest)
    assertNull(api.authTokens.value)
  }
}
