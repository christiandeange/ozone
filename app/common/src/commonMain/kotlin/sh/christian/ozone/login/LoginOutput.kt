package sh.christian.ozone.login

import sh.christian.ozone.login.auth.AuthInfo

sealed interface LoginOutput {
  object CanceledLogin : LoginOutput

  data class LoggedIn(
    val authInfo: AuthInfo,
  ) : LoginOutput
}
