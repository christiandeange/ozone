package sh.christian.ozone.app

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import sh.christian.ozone.app.AppState.ShowingLoggedIn
import sh.christian.ozone.app.AppState.ShowingLogin
import sh.christian.ozone.login.LoginOutput.CanceledLogin
import sh.christian.ozone.login.LoginOutput.LoggedIn
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.login.LoginWorkflow

class AppWorkflow(
  private val loginRepository: LoginRepository,
  private val loginWorkflow: LoginWorkflow,
  private val loggedInWorkflow: LoggedInWorkflow,
) : StatefulWorkflow<Unit, AppState, Unit, AppScreen>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): AppState {
    val authInfo = loginRepository.auth
    return if (authInfo == null) {
      ShowingLogin
    } else {
      ShowingLoggedIn(LoggedInProps(authInfo))
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
              state = ShowingLoggedIn(LoggedInProps(output.authInfo))
            }

            is CanceledLogin -> setOutput(Unit)
          }
        }
      }
    }
    is ShowingLoggedIn -> {
      context.renderChild(loggedInWorkflow, renderState.props) { output ->
        action {
          when (output) {
            LoggedInOutput.CloseApp -> setOutput(Unit)
            LoggedInOutput.SignOut -> {
              state = ShowingLogin
            }
          }
        }
      }
    }
  }

  override fun snapshotState(state: AppState): Snapshot? = null
}
