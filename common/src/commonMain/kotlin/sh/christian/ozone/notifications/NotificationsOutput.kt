package sh.christian.ozone.notifications

import sh.christian.ozone.home.HomeSubDestination

sealed interface NotificationsOutput {
  data class EnterScreen(
    val dest: HomeSubDestination,
  ) : NotificationsOutput

  object CloseApp : NotificationsOutput
}
