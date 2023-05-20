package sh.christian.ozone.app

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.runningWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.app.AppState.ShowingLoggedIn
import sh.christian.ozone.app.AppState.ShowingLogin
import sh.christian.ozone.home.HomeOutput
import sh.christian.ozone.home.HomeProps
import sh.christian.ozone.home.HomeWorkflow
import sh.christian.ozone.login.LoginOutput.CanceledLogin
import sh.christian.ozone.login.LoginOutput.LoggedIn
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.login.LoginWorkflow
import sh.christian.ozone.notifications.NotificationsRepository

@Inject
class AppWorkflow(
  private val loginRepository: LoginRepository,
  private val loginWorkflow: LoginWorkflow,
  private val homeWorkflow: HomeWorkflow,
  private val notificationsRepository: NotificationsRepository,
) : StatefulWorkflow<Unit, AppState, Unit, AppScreen>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): AppState {
    val authInfo = runBlocking { loginRepository.auth().first() }
    return if (authInfo == null) {
      ShowingLogin
    } else {
      ShowingLoggedIn(HomeProps(authInfo, 0))
    }
  }

  override fun render(
    renderProps: Unit,
    renderState: AppState,
    context: RenderContext,
  ): AppScreen = when (renderState) {
    is ShowingLogin -> {
      context.renderChild(loginWorkflow) { output ->
        action {
          when (output) {
            is LoggedIn -> {
              loginRepository.auth = output.authInfo
              state = ShowingLoggedIn(HomeProps(output.authInfo, 0))
            }

            is CanceledLogin -> setOutput(Unit)
          }
        }
      }
    }
    is ShowingLoggedIn -> {
      context.runningWorker(notificationsRepository.unreadCount.asWorker()) { unread ->
        action {
          state = ShowingLoggedIn(renderState.props.copy(unreadNotificationCount = unread))
        }
      }

      context.renderChild(homeWorkflow, renderState.props) { output ->
        action {
          when (output) {
            is HomeOutput.CloseApp -> setOutput(Unit)
            is HomeOutput.SignOut -> {
              loginRepository.auth = null
              state = ShowingLogin
            }
          }
        }
      }
    }
  }

  override fun snapshotState(state: AppState): Snapshot? = null
}
