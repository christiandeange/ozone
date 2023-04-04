package sh.christian.ozone.login

import com.atproto.server.CreateSessionRequest
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.NetworkWorker
import sh.christian.ozone.api.ServerRepository
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.AppScreen
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
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable.DismissHandler

class LoginWorkflow(
  private val serverRepository: ServerRepository,
  private val apiRepository: ApiProvider,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<Unit, LoginState, LoginOutput, AppScreen>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): LoginState = ShowingLogin

  override fun render(
    renderProps: Unit,
    renderState: LoginState,
    context: RenderContext,
  ): AppScreen = when (renderState) {
    is ShowingLogin -> {
      AppScreen(context.loginScreen())
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
              setOutput(LoggedIn(authInfo))
            }
            is AtpResponse.Failure -> {
              val errorProps = result.toErrorProps(true)
                ?: CustomError("Oops.", "Something bad happened.", false)

              state = ShowingError(errorProps, renderState.credentials)
            }
          }
        }
      }

      AppScreen(
        main = context.loginScreen(),
        overlay = TextOverlayScreen(
          onDismiss = DismissHandler(context.eventHandler {
            state = ShowingLogin
          }),
          text = "Signing in as ${renderState.credentials.username}...",
        )
      )
    }
    is ShowingError -> {
      AppScreen(
        main = context.loginScreen(),
        overlay = context.renderChild(errorWorkflow, renderState.errorProps) { output ->
          action {
            state = when (output) {
              ErrorOutput.Dismiss -> ShowingLogin
              ErrorOutput.Retry -> SigningIn(renderState.credentials)
            }
          }
        }
      )
    }
  }

  override fun snapshotState(state: LoginState): Snapshot? = null

  private fun RenderContext.loginScreen(): LoginScreen {
    return LoginScreen(
      server = serverRepository.server,
      onChangeServer = eventHandler { server ->
        serverRepository.server = server
      },
      onExit = eventHandler {
        setOutput(CanceledLogin)
      },
      onLogin = eventHandler { credentials ->
        state = SigningIn(credentials)
      },
    )
  }

  private fun signIn(credentials: Credentials) = NetworkWorker {
    apiRepository.api.createSession(
      CreateSessionRequest(credentials.username, credentials.password)
    )
  }
}
