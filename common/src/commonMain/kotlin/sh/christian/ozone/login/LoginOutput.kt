package sh.christian.ozone.login

sealed interface LoginOutput {
  object CanceledLogin : LoginOutput

  data class LoggedIn(
    val token: String,
  ) : LoginOutput
}
