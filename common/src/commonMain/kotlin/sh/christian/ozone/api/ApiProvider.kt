package sh.christian.ozone.api

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
import sh.christian.ozone.api.xrpc.Tokens
import sh.christian.ozone.api.xrpc.XrpcApi
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

  private val host = MutableStateFlow(apiRepository.server!!.host)
  private val auth = MutableStateFlow(loginRepository.auth)
  private val tokens = MutableStateFlow(loginRepository.auth?.toTokens())

  private val _api: XrpcApi = XrpcApi(host, tokens)
  val api: AtpApi get() = _api

  override suspend fun CoroutineScope.onStart() {
    coroutineScope {
      launch(Dispatchers.IO) {
        apiRepository.server().map { it.host }
          .distinctUntilChanged()
          .collect(host)
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
