package sh.christian.ozone.home

import sh.christian.ozone.compose.ComposePostProps
import sh.christian.ozone.profile.ProfileProps
import sh.christian.ozone.thread.ThreadProps

sealed interface HomeSubDestination {
  data class GoToProfile(
    val props: ProfileProps,
  ) : HomeSubDestination

  data class GoToThread(
    val props: ThreadProps,
  ) : HomeSubDestination

  data class GoToComposePost(
    val props: ComposePostProps,
  ) : HomeSubDestination
}
