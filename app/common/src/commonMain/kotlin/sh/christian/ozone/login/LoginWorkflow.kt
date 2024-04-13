package sh.christian.ozone.login

import com.atproto.server.CreateAccountRequest
import com.atproto.server.CreateSessionRequest
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.NetworkWorker
import sh.christian.ozone.api.ServerRepository
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.login.LoginOutput.CanceledLogin
import sh.christian.ozone.login.LoginOutput.LoggedIn
import sh.christian.ozone.login.LoginState.ShowingError
import sh.christian.ozone.login.LoginState.ShowingLogin
import sh.christian.ozone.login.LoginState.SigningIn
import sh.christian.ozone.login.auth.AuthInfo
import sh.christian.ozone.login.auth.Credentials
import sh.christian.ozone.login.auth.Server
import sh.christian.ozone.login.auth.ServerInfo
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable.DismissHandler

@Inject
class LoginWorkflow(
  private val serverRepository: ServerRepository,
  private val apiProvider: ApiProvider,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<Unit, LoginState, LoginOutput, AppScreen>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): LoginState = ShowingLogin(
    mode = LoginScreenMode.SIGN_IN,
    serverInfo = null,
  )

  override fun render(
    renderProps: Unit,
    renderState: LoginState,
    context: RenderContext,
  ): AppScreen {
    val server = serverRepository.server
    context.runningWorker(serverInfo(), "server-info-${server}") { response ->
      action {
        val maybeServerInfo = response.maybeResponse()
        state = when (val currentState = state) {
          is ShowingLogin -> currentState.copy(serverInfo = maybeServerInfo)
          is SigningIn -> currentState.copy(serverInfo = maybeServerInfo)
          is ShowingError -> currentState.copy(serverInfo = maybeServerInfo)
        }
      }
    }

    val loginScreen = context.loginScreen(renderState.mode, server, renderState.serverInfo)

    return when (renderState) {
      is ShowingLogin -> {
        AppScreen(main = loginScreen)
      }
      is SigningIn -> {
        val credentials: Credentials = renderState.credentials
        context.runningWorker(signIn(renderState.mode, credentials)) { result ->
          action {
            when (result) {
              is AtpResponse.Success -> {
                setOutput(LoggedIn(result.response))
              }
              is AtpResponse.Failure -> {
                val errorProps = result.toErrorProps(true)
                  ?: ErrorProps("Oops.", "Something bad happened.", false)

                state = ShowingError(state.mode, state.serverInfo, errorProps, credentials)
              }
            }
          }
        }

        AppScreen(
          main = loginScreen,
          overlay = TextOverlayScreen(
            onDismiss = DismissHandler(context.eventHandler {
              state = ShowingLogin(mode = state.mode, serverInfo = state.serverInfo)
            }),
            text = "Signing in as ${credentials.username}...",
          )
        )
      }
      is ShowingError -> {
        AppScreen(
          main = loginScreen,
          overlay = context.renderChild(errorWorkflow, renderState.errorProps) { output ->
            action {
              state = when (output) {
                ErrorOutput.Dismiss -> ShowingLogin(
                  mode = state.mode, serverInfo = state.serverInfo
                )
                ErrorOutput.Retry -> SigningIn(
                  state.mode, state.serverInfo, renderState.credentials
                )
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: LoginState): Snapshot? = null

  private fun RenderContext.loginScreen(
    mode: LoginScreenMode,
    server: Server,
    serverInfo: ServerInfo?,
  ): LoginScreen {
    return LoginScreen(
      mode = mode,
      onChangeMode = eventHandler { newMode ->
        state = when (val currentState = state) {
          is ShowingError -> currentState.copy(mode = newMode)
          is ShowingLogin -> currentState.copy(mode = newMode)
          is SigningIn -> currentState.copy(mode = newMode)
        }
      },
      server = server,
      serverInfo = serverInfo,
      onChangeServer = eventHandler { newServer ->
        serverRepository.server = newServer
      },
      onExit = eventHandler {
        setOutput(CanceledLogin)
      },
      onLogin = eventHandler { credentials ->
        state = SigningIn(state.mode, state.serverInfo, credentials)
      },
    )
  }

  private fun serverInfo(): NetworkWorker<ServerInfo> {
    return NetworkWorker {
      apiProvider.api.describeServer().map { response ->
        ServerInfo(
          inviteCodeRequired = response.inviteCodeRequired ?: false,
          availableUserDomains = response.availableUserDomains,
          privacyPolicy = response.links?.privacyPolicy?.uri,
          termsOfService = response.links?.termsOfService?.uri,
        )
      }
    }
  }

  private fun signIn(
    mode: LoginScreenMode,
    credentials: Credentials,
  ): NetworkWorker<AuthInfo> = NetworkWorker {
    when (mode) {
      LoginScreenMode.SIGN_UP -> {
        val request = CreateAccountRequest(
          email = credentials.email!!,
          handle = credentials.username,
          inviteCode = credentials.inviteCode,
          password = credentials.password,
          recoveryKey = null,
        )
        apiProvider.api.createAccount(request).map { response ->
          AuthInfo(
            accessJwt = response.accessJwt,
            refreshJwt = response.refreshJwt,
            handle = response.handle,
            did = response.did,
          )
        }
      }
      LoginScreenMode.SIGN_IN -> {
        val request = CreateSessionRequest(credentials.username.handle, credentials.password)
        apiProvider.api.createSession(request).map { response ->
          AuthInfo(
            accessJwt = response.accessJwt,
            refreshJwt = response.refreshJwt,
            handle = response.handle,
            did = response.did,
          )
        }
      }
    }
  }
}
