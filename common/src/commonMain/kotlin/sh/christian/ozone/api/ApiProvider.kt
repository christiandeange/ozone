package sh.christian.ozone.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sh.christian.ozone.api.xrpc.XrpcApi
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.login.LoginRepository

class ApiProvider(
  private val apiRepository: ServerRepository,
  private val loginRepository: LoginRepository,
) : Supervisor {

  private val _api = XrpcApi(apiRepository.server.host, loginRepository.auth?.accessJwt)
  val api: AtpApi = runBlocking(Dispatchers.IO) { _api }

  override fun CoroutineScope.onStart() {
    launch(Dispatchers.Unconfined) {
      combine(apiRepository.server(), loginRepository.auth()) { server, auth ->
        _api.host = server.host
        _api.authorizationToken = auth?.accessJwt
      }.collect()
    }
  }
}
