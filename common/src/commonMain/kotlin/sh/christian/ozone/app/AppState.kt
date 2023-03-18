package sh.christian.ozone.app

sealed interface AppState {
  object ShowingLogin : AppState

  data class ShowingLoggedIn(
    val props: LoggedInProps,
  ) : AppState
}
