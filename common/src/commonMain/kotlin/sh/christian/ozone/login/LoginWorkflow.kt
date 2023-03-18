package sh.christian.ozone.login

import com.atproto.session.CreateRequest
import com.atproto.session.CreateResponse
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.ServerRepository
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorProps.CustomError
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.login.LoginOutput.CanceledLogin
import sh.christian.ozone.login.LoginOutput.LoggedIn
import sh.christian.ozone.login.LoginState.ShowingError
import sh.christian.ozone.login.LoginState.ShowingLogin
import sh.christian.ozone.login.LoginState.SigningIn
import sh.christian.ozone.login.auth.AuthInfo
import sh.christian.ozone.login.auth.Credentials
import sh.christian.ozone.ui.compose.Dismissable.DismissHandler
import sh.christian.ozone.ui.compose.OverlayScreen
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.plus

class LoginWorkflow(
  private val loginRepository: LoginRepository,
  private val serverRepository: ServerRepository,
  private val apiRepository: ApiProvider,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<Unit, LoginState, LoginOutput, ViewRendering>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): LoginState = ShowingLogin

  override fun render(
    renderProps: Unit,
    renderState: LoginState,
    context: RenderContext,
  ): ViewRendering = when (renderState) {
    is ShowingLogin -> {
      context.loginScreen()
    }
    is SigningIn -> {
      context.runningWorker(signIn(renderState.credentials)) { result ->
        action {
          when (result) {
            is AtpResponse.Success -> {
              val authInfo = AuthInfo(
                accessJwt = result.response.accessJwt,
                refreshJwt = result.response.refreshJwt,
                handle = result.response.handle,
                did = result.response.did,
              )
              loginRepository.auth = authInfo
              setOutput(LoggedIn(authInfo))
            }
            is AtpResponse.Failure -> {
              val errorProps = result.toErrorProps(true)
                ?: CustomError("Oops.", "Something bad happened", false)

              state = ShowingError(errorProps, renderState.credentials)
            }
          }
        }
      }

      context.loginScreen() + OverlayScreen(
        text = "Signing in as ${renderState.credentials.username}...",
        onDismiss = DismissHandler(context.eventHandler {
          state = ShowingLogin
        }),
      )
    }
    is ShowingError -> {
      context.loginScreen() + context.renderChild(errorWorkflow, renderState.errorProps) { output ->
        action {
          state = when (output) {
            ErrorOutput.Dismiss -> ShowingLogin
            ErrorOutput.Retry -> SigningIn(renderState.credentials)
          }
        }
      }
    }
  }

  override fun snapshotState(state: LoginState): Snapshot? = null

  private fun RenderContext.loginScreen(): LoginScreen {
    return LoginScreen(
      server = serverRepository.server,
      onChangeServer = eventHandler { server ->
        serverRepository.server = server
      },
      onCancel = eventHandler {
        setOutput(CanceledLogin)
      },
      onLogin = eventHandler { credentials ->
        state = SigningIn(credentials)
      },
    )
  }

  private fun signIn(credentials: Credentials): Worker<AtpResponse<CreateResponse>> = Worker.from {
    apiRepository.api.createSession(CreateRequest(credentials.username, credentials.password))
  }
}
