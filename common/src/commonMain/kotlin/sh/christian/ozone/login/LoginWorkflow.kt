package sh.christian.ozone.login

import com.atproto.server.CreateAccountRequest
import com.atproto.server.CreateSessionRequest
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import me.tatarka.inject.annotations.Inject
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
import sh.christian.ozone.login.SavedCredentials.DoNotFetchSavedCredentials
import sh.christian.ozone.login.SavedCredentials.FetchSavedCredentials
import sh.christian.ozone.login.SavedCredentials.LoadedSavedCredentials
import sh.christian.ozone.login.auth.AuthInfo
import sh.christian.ozone.login.auth.CredentialManager
import sh.christian.ozone.login.auth.Credentials
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable.DismissHandler

@Inject
class LoginWorkflow(
  private val credentialManager: CredentialManager,
  private val serverRepository: ServerRepository,
  private val apiProvider: ApiProvider,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<Unit, LoginState, LoginOutput, AppScreen>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): LoginState = ShowingLogin(
    mode = LoginScreenMode.SIGN_IN,
    savedCredentials = FetchSavedCredentials,
  )

  override fun render(
    renderProps: Unit,
    renderState: LoginState,
    context: RenderContext,
  ): AppScreen = when (renderState) {
    is ShowingLogin -> {
      if (renderState.savedCredentials is FetchSavedCredentials) {
        val savedCredentialsWorker = Worker.from { credentialManager.getStoredCredentials() }
        context.runningWorker(savedCredentialsWorker) { savedCredentials ->
          action {
            state = ShowingLogin(state.mode, LoadedSavedCredentials(savedCredentials))
          }
        }
      }

      AppScreen(main = context.loginScreen(renderState))
    }

    is SigningIn -> {
      context.runningWorker(signIn(renderState.mode, renderState.credentials)) { result ->
        action {
          when (result) {
            is AtpResponse.Success -> {
              setOutput(LoggedIn(result.response))
            }

            is AtpResponse.Failure -> {
              val errorProps = result.toErrorProps(true)
                ?: CustomError("Oops.", "Something bad happened.", false)

              state = ShowingError(state.mode, errorProps, renderState.credentials)
            }
          }
        }
      }

      AppScreen(
        main = context.loginScreen(renderState),
        overlay = TextOverlayScreen(
          onDismiss = DismissHandler(context.eventHandler {
            state = ShowingLogin(state.mode, DoNotFetchSavedCredentials)
          }),
          text = "Signing in as ${renderState.credentials.username}...",
        )
      )
    }

    is ShowingError -> {
      AppScreen(
        main = context.loginScreen(renderState),
        overlay = context.renderChild(errorWorkflow, renderState.errorProps) { output ->
          action {
            state = when (output) {
              ErrorOutput.Dismiss -> ShowingLogin(state.mode, DoNotFetchSavedCredentials)
              ErrorOutput.Retry -> SigningIn(state.mode, renderState.credentials)
            }
          }
        }
      )
    }
  }

  override fun snapshotState(state: LoginState): Snapshot? = null

  private fun RenderContext.loginScreen(renderState: LoginState): LoginScreen {
    return LoginScreen(
      api = apiProvider.api,
      mode = renderState.mode,
      saved = when (val saved = (renderState as? ShowingLogin)?.savedCredentials) {
        is FetchSavedCredentials,
        is DoNotFetchSavedCredentials,
        null -> null

        is LoadedSavedCredentials -> saved.credentials
      },
      onChangeMode = eventHandler { newMode ->
        state = when (val currentState = state) {
          is ShowingError -> currentState.copy(mode = newMode)
          is ShowingLogin -> currentState.copy(mode = newMode)
          is SigningIn -> currentState.copy(mode = newMode)
        }
      },
      server = serverRepository.server!!,
      onChangeServer = eventHandler { server ->
        serverRepository.server = server
      },
      onExit = eventHandler {
        setOutput(CanceledLogin)
      },
      onLogin = eventHandler { credentials ->
        state = SigningIn(renderState.mode, credentials)
      },
    )
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
        val request = CreateSessionRequest(credentials.username, credentials.password)
        apiProvider.api.createSession(request).map { response ->
          AuthInfo(
            accessJwt = response.accessJwt,
            refreshJwt = response.refreshJwt,
            handle = response.handle,
            did = response.did,
          )
        }
      }
    }.also { response ->
      if (response is AtpResponse.Success) {
        credentialManager.saveCredentials(credentials)
      }
    }
  }
}
