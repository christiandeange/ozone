package sh.christian.ozone.app

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import sh.christian.ozone.app.AppState.ShowingLoggedIn
import sh.christian.ozone.app.AppState.ShowingLogin
import sh.christian.ozone.login.LoginOutput.CanceledLogin
import sh.christian.ozone.login.LoginOutput.LoggedIn
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.login.LoginWorkflow
import sh.christian.ozone.timeline.TimelineOutput
import sh.christian.ozone.timeline.TimelineProps
import sh.christian.ozone.timeline.TimelineWorkflow

class AppWorkflow(
  private val loginRepository: LoginRepository,
  private val loginWorkflow: LoginWorkflow,
  private val timelineWorkflow: TimelineWorkflow,
) : StatefulWorkflow<Unit, AppState, Unit, AppScreen>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): AppState {
    val authInfo = runBlocking { loginRepository.auth().first() }
    return if (authInfo == null) {
      ShowingLogin
    } else {
      ShowingLoggedIn(TimelineProps(authInfo))
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
              state = ShowingLoggedIn(TimelineProps(output.authInfo))
            }

            is CanceledLogin -> setOutput(Unit)
          }
        }
      }
    }
    is ShowingLoggedIn -> {
      context.renderChild(timelineWorkflow, renderState.props) { output ->
        action {
          when (output) {
            TimelineOutput.CloseApp -> setOutput(Unit)
            TimelineOutput.SignOut -> {
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
