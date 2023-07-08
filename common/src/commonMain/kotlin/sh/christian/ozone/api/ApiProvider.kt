package sh.christian.ozone.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import me.tatarka.inject.annotations.Inject
import sh.christian.atp.api.AtpApi
import sh.christian.atp.api.XrpcApi
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.di.SingleInApp
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.login.auth.AuthInfo

@Inject
@SingleInApp
class ApiProvider(
  private val apiRepository: ServerRepository,
  private val loginRepository: LoginRepository,
) : Supervisor {

  private val apiHost = MutableStateFlow(apiRepository.server!!.host)
  private val auth = MutableStateFlow(loginRepository.auth)
  private val tokens = MutableStateFlow(loginRepository.auth?.toTokens())

  private val client = HttpClient(CIO) {
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.INFO
    }

    install(XrpcAuthPlugin) {
      authTokens = tokens
    }

    install(DefaultRequest) {
      val hostUrl = Url(apiHost.value)
      url.protocol = hostUrl.protocol
      url.host = hostUrl.host
      url.port = hostUrl.port
    }

    expectSuccess = false
  }

  val api: AtpApi = XrpcApi(client)

  override suspend fun CoroutineScope.onStart() {
    coroutineScope {
      launch(Dispatchers.IO) {
        apiRepository.server().map { it.host }
          .distinctUntilChanged()
          .collect(apiHost)
      }

      launch(Dispatchers.IO) {
        loginRepository.auth()
          .distinctUntilChanged()
          .collect {
            tokens.value = it?.toTokens()
            yield()
            auth.value = it
          }
      }

      launch(Dispatchers.IO) {
        tokens.collect { tokens ->
          if (tokens != null) {
            loginRepository.auth = loginRepository.auth().first()!!.withTokens(tokens)
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
