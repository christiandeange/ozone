package sh.christian.ozone.api.xrpc

import com.atproto.session.RefreshResponse
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.DefaultJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

internal fun HttpClientConfig<*>.XrpcAuth(
  json: Json = DefaultJson,
  tokens: MutableStateFlow<Tokens?>,
) {
  val refreshPath = "/xrpc/com.atproto.session.refresh"

  install(Auth) {
    bearer {
      sendWithoutRequest {
        it.url.encodedPath != refreshPath
      }

      loadTokens {
        tokens.value?.let { tokens ->
          BearerTokens(accessToken = tokens.auth, refreshToken = tokens.refresh)
        }
      }

      refreshTokens {
        client
          .post(refreshPath) { bearerAuth(oldTokens!!.refreshToken) }
          .toAtpResponse<RefreshResponse>()
          .maybeResponse()
          ?.let { response ->
            tokens.value = Tokens(response.accessJwt, response.refreshJwt)
            BearerTokens(response.accessJwt, response.refreshJwt)
          }
      }
    }
  }
  install(FixExpiredTokenErrorPlugin) {
    this.json = json
  }
}

data class Tokens(
  val auth: String,
  val refresh: String,
)
