package sh.christian.ozone.login

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import sh.christian.ozone.login.LoginOutput.CanceledLogin
import sh.christian.ozone.login.LoginState.ShowingLogin
import sh.christian.ozone.ui.workflow.ViewRendering

class LoginWorkflow : StatefulWorkflow<Unit, LoginState, LoginOutput, ViewRendering>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): LoginState = ShowingLogin

  override fun render(
    renderProps: Unit,
    renderState: LoginState,
    context: RenderContext,
  ): ViewRendering = when (renderState) {
    ShowingLogin -> {
      LoginScreen(
        onCancel = context.eventHandler {
          setOutput(CanceledLogin)
        },
        onLogin = context.eventHandler { credentials ->
        },
      )
    }
  }

  override fun snapshotState(state: LoginState): Snapshot? = null
}
