package sh.christian.ozone.app

import sh.christian.ozone.home.HomeProps

sealed interface AppState {
  object ShowingLogin : AppState

  data class ShowingLoggedIn(
    val props: HomeProps,
  ) : AppState
}
