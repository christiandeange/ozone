package sh.christian.ozone.home

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.WorkflowAction
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.compose.ComposePostWorkflow
import sh.christian.ozone.home.HomeState.InSubScreen
import sh.christian.ozone.home.HomeState.InTab
import sh.christian.ozone.home.HomeSubDestination.GoToComposePost
import sh.christian.ozone.home.HomeSubDestination.GoToProfile
import sh.christian.ozone.home.HomeSubDestination.GoToThread
import sh.christian.ozone.home.SelectedHomeScreenTab.NOTIFICATIONS
import sh.christian.ozone.home.SelectedHomeScreenTab.SETTINGS
import sh.christian.ozone.home.SelectedHomeScreenTab.TIMELINE
import sh.christian.ozone.notifications.NotificationsWorkflow
import sh.christian.ozone.profile.ProfileWorkflow
import sh.christian.ozone.settings.SettingsOutput
import sh.christian.ozone.settings.SettingsWorkflow
import sh.christian.ozone.thread.ThreadWorkflow
import sh.christian.ozone.timeline.TimelineOutput
import sh.christian.ozone.timeline.TimelineProps
import sh.christian.ozone.timeline.TimelineWorkflow

class HomeWorkflow(
  private val timelineWorkflow: TimelineWorkflow,
  private val notificationsWorkflow: NotificationsWorkflow,
  private val settingsWorkflow: SettingsWorkflow,
  private val profileWorkflow: ProfileWorkflow,
  private val threadWorkflow: ThreadWorkflow,
  private val composePostWorkflow: ComposePostWorkflow,
) : StatefulWorkflow<HomeProps, HomeState, HomeOutput, AppScreen>() {
  override fun initialState(
    props: HomeProps,
    snapshot: Snapshot?
  ): HomeState {
    return InTab.InTimeline(TimelineProps(props.authInfo))
  }

  override fun render(
    renderProps: HomeProps,
    renderState: HomeState,
    context: RenderContext
  ): AppScreen {
    val tabState = when (renderState) {
      is InTab -> renderState
      is InSubScreen -> renderState.inTabState
    }

    val tabScreen: AppScreen = when (tabState) {
      is InTab.InTimeline -> {
        context.renderChild(timelineWorkflow, tabState.props) { output ->
          action {
            when (output) {
              is TimelineOutput.EnterScreen -> {
                state = output.dest.destinationState(tabState)
              }
              is TimelineOutput.CloseApp -> setOutput(HomeOutput.CloseApp)
            }
          }
        }
      }
      is InTab.InNotifications -> {
        context.renderChild(notificationsWorkflow) {
          action {
            setOutput(HomeOutput.CloseApp)
          }
        }
      }
      is InTab.InSettings -> {
        context.renderChild(settingsWorkflow) { output ->
          action {
            when (output) {
              is SettingsOutput.CloseApp -> setOutput(HomeOutput.CloseApp)
              is SettingsOutput.SignOut -> setOutput(HomeOutput.SignOut)
            }
          }
        }
      }
    }

    val homeScreen = HomeScreen(
      homeContent = tabScreen.mains,
      unreadCount = when (val unread = renderProps.unreadNotificationCount) {
        0 -> null
        in 1..99 -> unread.toString()
        else -> "💯"
      },
      tab = when (tabState) {
        is InTab.InTimeline -> TIMELINE
        is InTab.InNotifications -> NOTIFICATIONS
        is InTab.InSettings -> SETTINGS
      },
      onChangeTab = context.eventHandler { tab ->
        state = when (tab) {
          TIMELINE -> InTab.InTimeline(TimelineProps(props.authInfo))
          NOTIFICATIONS -> InTab.InNotifications
          SETTINGS -> InTab.InSettings
        }
      },
      onExit = context.eventHandler { setOutput(HomeOutput.CloseApp) },
    )

    return when (renderState) {
      is InTab -> {
        AppScreen(
          main = homeScreen,
          overlay = tabScreen.overlay,
        )
      }
      is InSubScreen -> {
        val handler: (Any?) -> WorkflowAction<HomeProps, HomeState, HomeOutput> = {
          action { state = renderState.inTabState }
        }
        val subScreen: AppScreen = when (renderState) {
          is InSubScreen.InProfile -> {
            context.renderChild(profileWorkflow, renderState.props, handler = handler)
          }
          is InSubScreen.InThread -> {
            context.renderChild(threadWorkflow, renderState.props, handler = handler)
          }
          is InSubScreen.InComposePost -> {
            context.renderChild(composePostWorkflow, renderState.props, handler = handler)
          }
        }

        AppScreen(
          mains = listOf(homeScreen) + subScreen.mains,
          overlay = subScreen.overlay,
        )
      }
    }
  }

  override fun snapshotState(state: HomeState): Snapshot? = null
}

private fun HomeSubDestination.destinationState(tabState: InTab): InSubScreen {
  return when (this) {
    is GoToProfile -> InSubScreen.InProfile(props, tabState)
    is GoToThread -> InSubScreen.InThread(props, tabState)
    is GoToComposePost -> InSubScreen.InComposePost(props, tabState)
  }
}
