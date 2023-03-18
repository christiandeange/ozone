package sh.christian.ozone.login

import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.login.auth.Credentials

sealed interface LoginState {
  object ShowingLogin : LoginState

  data class SigningIn(
    val credentials: Credentials,
  ) : LoginState

  data class ShowingError(
    val errorProps: ErrorProps,
    val credentials: Credentials,
  ) : LoginState
}
