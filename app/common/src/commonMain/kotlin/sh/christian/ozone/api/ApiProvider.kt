package sh.christian.ozone.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.takeFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.BlueskyApi
import sh.christian.ozone.XrpcBlueskyApi
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.di.SingleInApp
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.login.auth.AuthInfo

@Inject
@SingleInApp
class ApiProvider(
  private val apiRepository: ServerRepository,
  private val loginRepository: LoginRepository,
) : Supervisor() {

  private val apiHost = MutableStateFlow(apiRepository.server.host)
  private val auth = MutableStateFlow(loginRepository.auth)
  private val tokens = MutableStateFlow(loginRepository.auth?.toTokens())

  private val client = HttpClient(engine) {
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.NONE
    }

    install(XrpcAuthPlugin) {
      authTokens = tokens
    }

    install(DefaultRequest) {
      url.takeFrom(apiHost.value)
    }

    expectSuccess = false
  }

  val api: BlueskyApi = XrpcBlueskyApi(client)

  override suspend fun CoroutineScope.onStart() {
    coroutineScope {
      launch(OzoneDispatchers.IO) {
        apiRepository.serverFlow().map { it.host }
          .distinctUntilChanged()
          .collect(apiHost)
      }

      launch(OzoneDispatchers.IO) {
        loginRepository.authFlow().collect {
            tokens.value = it?.toTokens()
            yield()
            auth.value = it
          }
      }

      launch(OzoneDispatchers.IO) {
        tokens.collect { tokens ->
          if (tokens != null) {
            loginRepository.auth = loginRepository.auth?.withTokens(tokens)
          } else {
            loginRepository.auth = null
          }
        }
      }
    }
  }

  fun auth(): Flow<AuthInfo?> = auth

  private fun AuthInfo.toTokens() = Tokens(accessJwt, refreshJwt)

  private fun AuthInfo.withTokens(tokens: Tokens) = copy(
    accessJwt = tokens.auth,
    refreshJwt = tokens.refresh,
  )
}
