package sh.christian.ozone.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sh.christian.ozone.api.xrpc.Tokens
import sh.christian.ozone.api.xrpc.XrpcApi
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.login.auth.AuthInfo

class ApiProvider(
  private val apiRepository: ServerRepository,
  private val loginRepository: LoginRepository,
) : Supervisor {

  private lateinit var _api: XrpcApi
  val api: AtpApi get() = runBlocking(Dispatchers.IO) { _api }

  override suspend fun CoroutineScope.onStart() {
    val host = apiRepository.server().map { it.host }.stateIn(this)
    val auth = MutableStateFlow(loginRepository.auth().first()?.toTokens())
    _api = XrpcApi(host, auth)

    coroutineScope {
      launch(Dispatchers.IO) {
        loginRepository.auth().map { it?.toTokens() }
          .distinctUntilChanged()
          .collect(auth)
      }

      launch(Dispatchers.IO) {
        auth.collect { tokens ->
          if (tokens != null) {
            loginRepository.auth = loginRepository.auth().first()!!.withTokens(tokens)
          } else {
            loginRepository.auth = null
          }
        }
      }
    }
  }

  private fun AuthInfo.toTokens() = Tokens(accessJwt, refreshJwt)

  private fun AuthInfo.withTokens(tokens: Tokens) = copy(
    accessJwt = tokens.auth,
    refreshJwt = tokens.refresh,
  )
}
