package sh.christian.ozone.app

import sh.christian.ozone.timeline.TimelineProps

sealed interface AppState {
  object ShowingLogin : AppState

  data class ShowingLoggedIn(
    val props: TimelineProps,
  ) : AppState
}
