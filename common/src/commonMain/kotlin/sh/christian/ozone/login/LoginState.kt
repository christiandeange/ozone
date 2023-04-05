package sh.christian.ozone.login

import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.login.auth.Credentials

sealed interface LoginState {
  val mode: LoginScreenMode

  data class ShowingLogin(
    override val mode: LoginScreenMode,
  ) : LoginState

  data class SigningIn(
    override val mode: LoginScreenMode,
    val credentials: Credentials,
  ) : LoginState

  data class ShowingError(
    override val mode: LoginScreenMode,
    val errorProps: ErrorProps,
    val credentials: Credentials,
  ) : LoginState
}
