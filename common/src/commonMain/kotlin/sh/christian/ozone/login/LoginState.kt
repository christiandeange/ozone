package sh.christian.ozone.login

sealed interface LoginState {
  object ShowingLogin : LoginState
}
