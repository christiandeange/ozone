package sh.christian.ozone.app

sealed interface AppState {
  object ShowingLogin : AppState
}
