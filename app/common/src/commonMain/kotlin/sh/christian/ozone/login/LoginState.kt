package sh.christian.ozone.login

import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.login.auth.Credentials
import sh.christian.ozone.login.auth.Server
import sh.christian.ozone.login.auth.ServerInfo

sealed interface LoginState {
  val mode: LoginScreenMode
  val serverInfo: ServerInfo?

  data class FetchingServer(
    override val mode: LoginScreenMode,
    override val serverInfo: ServerInfo?,
  ) : LoginState

  data class ShowingLogin(
    override val mode: LoginScreenMode,
    override val serverInfo: ServerInfo?,
    val server: Server,
  ) : LoginState

  data class SigningIn(
    override val mode: LoginScreenMode,
    override val serverInfo: ServerInfo?,
    val server: Server,
    val credentials: Credentials,
  ) : LoginState

  data class ShowingError(
    override val mode: LoginScreenMode,
    override val serverInfo: ServerInfo?,
    val server: Server,
    val errorProps: ErrorProps,
    val credentials: Credentials,
  ) : LoginState
}
