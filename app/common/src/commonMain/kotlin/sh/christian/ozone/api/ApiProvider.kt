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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.BlueskyApi
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

  private val client = HttpClient(engine) {
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.NONE
    }

    install(DefaultRequest) {
      url.takeFrom(apiHost.value)
    }

    expectSuccess = false
  }

  private val _api = AuthenticatedXrpcBlueskyApi(
    httpClient = client,
    initialTokens = loginRepository.auth?.toTokens(),
  )

  val api: BlueskyApi = _api

  override suspend fun CoroutineScope.onStart() {
    coroutineScope {
      launch(OzoneDispatchers.IO) {
        apiRepository.serverFlow().map { it.host }
          .distinctUntilChanged()
          .collect(apiHost)
      }

      launch(OzoneDispatchers.IO) {
        _api.authTokens.collect { tokens ->
          if (tokens != null) {
            loginRepository.auth = loginRepository.auth?.withTokens(tokens)
          } else {
            loginRepository.auth = null
          }
        }
      }
    }
  }

  fun signOut() {
    _api.clearCredentials()
  }

  private fun AuthInfo.toTokens() = BlueskyAuthPlugin.Tokens(accessJwt, refreshJwt)

  private fun AuthInfo.withTokens(tokens: BlueskyAuthPlugin.Tokens) = copy(
    accessJwt = tokens.auth,
    refreshJwt = tokens.refresh,
  )
}
