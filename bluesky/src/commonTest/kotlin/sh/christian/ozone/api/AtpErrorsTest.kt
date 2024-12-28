package sh.christian.ozone.api

import com.atproto.server.DescribeServerLinks
import com.atproto.server.DescribeServerResponse
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import kotlinx.coroutines.test.runTest
import sh.christian.ozone.XrpcBlueskyApi
import sh.christian.ozone.api.response.AtpErrorDescription
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.api.response.StatusCode.InvalidRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AtpErrorsTest {

  private val describeServerResponse = DescribeServerResponse(
    did = Did("did:web:bsky.social"),
    availableUserDomains = listOf(".bsky.social"),
    inviteCodeRequired = false,
    phoneVerificationRequired = true,
    links = DescribeServerLinks(
      privacyPolicy = Uri("https://blueskyweb.xyz/support/privacy-policy"),
      termsOfService = Uri("https://blueskyweb.xyz/support/tos"),
    ),
  )

  private val errorDescription = AtpErrorDescription(
    error = "AuthFactorTokenRequired",
    message = "Needs 2FA",
  )

  @Test
  fun testSuccessHasNoError() = runTest {
    val api = XrpcBlueskyApi(HttpClient(mockEngine(statusCode = OK, response = describeServerResponse)))

    val response = api.describeServer()
    assertIs<AtpResponse.Success<DescribeServerResponse>>(response)
    assertEquals(response.response, describeServerResponse)
  }

  @Test
  fun testEmptyError() = runTest {
    val api = XrpcBlueskyApi(HttpClient(mockEngine(statusCode = BadRequest, response = "")))

    val response = api.describeServer()
    assertIs<AtpResponse.Failure<DescribeServerResponse>>(response)
    assertNull(response.response)
    assertNull(response.error)
  }

  @Test
  fun testErrorKeepsStatusCode() = runTest {
    val api = XrpcBlueskyApi(HttpClient(mockEngine(statusCode = BadRequest, response = "")))

    val response = api.describeServer()
    assertIs<AtpResponse.Failure<DescribeServerResponse>>(response)
    assertEquals(InvalidRequest, response.statusCode)
  }

  @Test
  fun testErrorKeepsHeaders() = runTest {
    val api = XrpcBlueskyApi(HttpClient(mockEngine(statusCode = BadRequest, response = "")))

    val response = api.describeServer()
    assertIs<AtpResponse.Failure<DescribeServerResponse>>(response)
    assertEquals(mapOf("Content-Type" to "application/json"), response.headers)
  }

  @Test
  fun testFailureWithResponse() = runTest {
    val api = XrpcBlueskyApi(HttpClient(mockEngine(statusCode = BadRequest, response = describeServerResponse)))

    val response = api.describeServer()
    assertIs<AtpResponse.Failure<DescribeServerResponse>>(response)
    assertEquals(InvalidRequest, response.statusCode)
    assertNull(response.error)
    assertEquals(response.response, describeServerResponse)
  }

  @Test
  fun testFailureWithAtpErrorDescription() = runTest {
    val api = XrpcBlueskyApi(HttpClient(mockEngine(statusCode = BadRequest, error = errorDescription)))

    val response = api.describeServer()
    assertIs<AtpResponse.Failure<DescribeServerResponse>>(response)
    assertEquals(InvalidRequest, response.statusCode)
    assertEquals(response.error, errorDescription)
    assertNull(response.response)
  }
}
