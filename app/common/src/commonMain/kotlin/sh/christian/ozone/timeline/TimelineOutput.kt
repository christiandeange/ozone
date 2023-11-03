package sh.christian.ozone.timeline

import sh.christian.ozone.home.HomeSubDestination

sealed interface TimelineOutput {
  data class EnterScreen(
    val dest: HomeSubDestination,
  ) : TimelineOutput

  object CloseApp : TimelineOutput
}
