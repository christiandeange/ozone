package sh.christian.ozone.notifications

import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.model.Notifications

sealed interface NotificationsState {
  val notifications: Notifications

  data class ShowingNotifications(
    override val notifications: Notifications,
    val isLoading: Boolean,
  ) : NotificationsState

  data class ShowingError(
    override val notifications: Notifications,
    val props: ErrorProps,
  ) : NotificationsState
}
