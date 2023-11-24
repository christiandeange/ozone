package sh.christian.ozone.app

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.runningWorker
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
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
import sh.christian.ozone.login.auth.AuthInfo
import sh.christian.ozone.notifications.NotificationsRepository

@Inject
class AppWorkflow(
  private val loginRepository: LoginRepository,
  private val loginWorkflow: LoginWorkflow,
  private val homeWorkflow: HomeWorkflow,
  private val notificationsRepository: NotificationsRepository,
) : StatefulWorkflow<AuthInfo?, AppState, Unit, AppScreen>() {
  override fun initialState(
    props: AuthInfo?,
    snapshot: Snapshot?,
  ): AppState {
    return if (props == null) {
      ShowingLogin
    } else {
      ShowingLoggedIn(HomeProps(props, 0))
    }
  }

  override fun render(
    renderProps: AuthInfo?,
    renderState: AppState,
    context: RenderContext,
  ): AppScreen = when (renderState) {
    is ShowingLogin -> {
      context.runningWorker(loginRepository.auth().filterNotNull().asWorker(), "has-auth") { auth ->
        action {
          state = ShowingLoggedIn(HomeProps(auth, 0))
        }
      }

      context.renderChild(loginWorkflow) { output ->
        action {
          when (output) {
            is LoggedIn -> loginRepository.setAuth(output.authInfo)
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
      context.runningWorker(loginRepository.auth().filter { it == null }.asWorker(), "no-auth") {
        action {
          state = ShowingLogin
        }
      }

      context.renderChild(homeWorkflow, renderState.props) { output ->
        action {
          when (output) {
            is HomeOutput.CloseApp -> setOutput(Unit)
            is HomeOutput.SignOut -> loginRepository.setAuth(null)
          }
        }
      }
    }
  }

  override fun snapshotState(state: AppState): Snapshot? = null
}
