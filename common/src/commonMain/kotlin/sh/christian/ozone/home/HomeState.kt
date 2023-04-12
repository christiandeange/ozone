package sh.christian.ozone.home

import sh.christian.ozone.compose.ComposePostProps
import sh.christian.ozone.profile.ProfileProps
import sh.christian.ozone.thread.ThreadProps
import sh.christian.ozone.timeline.TimelineProps

sealed interface HomeState {

  sealed interface InTab : HomeState {
    data class InTimeline(
      val props: TimelineProps,
    ) : InTab

    object InNotifications : InTab

    object InSettings : InTab
  }

  sealed interface InSubScreen : HomeState {
    val inTabState: InTab

    data class InProfile(
      val props: ProfileProps,
      override val inTabState: InTab,
    ) : InSubScreen

    data class InThread(
      val props: ThreadProps,
      override val inTabState: InTab,
    ) : InSubScreen

    data class InComposePost(
      val props: ComposePostProps,
      override val inTabState: InTab,
    ) : InSubScreen
  }
}
