package sh.christian.ozone.notifications

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import kotlinx.datetime.Clock
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.model.Notifications
import sh.christian.ozone.notifications.NotificationsState.ShowingError
import sh.christian.ozone.notifications.NotificationsState.ShowingNotifications

class NotificationsWorkflow(
  private val clock: Clock,
  private val notificationsRepository: NotificationsRepository,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<Unit, NotificationsState, Unit, AppScreen>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): NotificationsState = ShowingNotifications(
    notifications = Notifications(emptyList(), null),
    isLoading = true,
  )

  override fun render(
    renderProps: Unit,
    renderState: NotificationsState,
    context: RenderContext,
  ): AppScreen {
    context.runningWorker(notificationsRepository.notifications.asWorker()) { notifications ->
      action {
        state = ShowingNotifications(notifications, isLoading = false)
      }
    }
    context.runningWorker(notificationsRepository.errors.asWorker()) { error ->
      action {
        state = ShowingError(state.notifications, error)
      }
    }

    val notificationsScreen = context.notificationsScreen(renderState.notifications)

    return when (renderState) {
      is ShowingNotifications -> {
        if (renderState.isLoading) {
          context.runningSideEffect("load-notifications") {
            if (renderState.notifications.list.isEmpty()) {
              notificationsRepository.refresh()
            } else {
              notificationsRepository.loadMore()
            }
          }
        }

        AppScreen(main = notificationsScreen)
      }
      is ShowingError -> {
        AppScreen(
          main = notificationsScreen,
          overlay = context.renderChild(errorWorkflow, renderState.props) { output ->
            action {
              state = when (output) {
                ErrorOutput.Dismiss -> ShowingNotifications(state.notifications, isLoading = false)
                ErrorOutput.Retry -> ShowingNotifications(state.notifications, isLoading = true)
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: NotificationsState): Snapshot? = null

  private fun RenderContext.notificationsScreen(
    notifications: Notifications,
  ): NotificationsScreen {
    return NotificationsScreen(
      now = clock.now(),
      notifications = notifications.list,
      onLoadMore = eventHandler {
        state = ShowingNotifications(
          notifications = notifications,
          isLoading = true,
        )
      },
      onExit = eventHandler {
        setOutput(Unit)
      },
    )
  }
}
