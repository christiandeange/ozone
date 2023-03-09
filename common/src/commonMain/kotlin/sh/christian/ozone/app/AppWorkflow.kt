package sh.christian.ozone.app

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import sh.christian.ozone.app.AppState.ShowingLogin
import sh.christian.ozone.login.LoginOutput.CanceledLogin
import sh.christian.ozone.login.LoginOutput.LoggedIn
import sh.christian.ozone.login.LoginWorkflow
import sh.christian.ozone.ui.workflow.ViewRendering

class AppWorkflow(
  private val loginWorkflow: LoginWorkflow,
) : StatefulWorkflow<Unit, AppState, Unit, ViewRendering>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): AppState = ShowingLogin

  override fun render(
    renderProps: Unit,
    renderState: AppState,
    context: RenderContext,
  ): ViewRendering = when (renderState) {
    ShowingLogin -> {
      context.renderChild(loginWorkflow) { output ->
        action {
          when (output) {
            is LoggedIn -> {
              println("Login token: ${output.token}")
              setOutput(Unit)
            }

            is CanceledLogin -> setOutput(Unit)
          }
        }
      }
    }
  }

  override fun snapshotState(state: AppState): Snapshot? = null
}
