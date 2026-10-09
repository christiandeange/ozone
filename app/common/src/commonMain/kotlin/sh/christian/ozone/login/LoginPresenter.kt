package sh.christian.ozone.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.bsky.feed.GetTimelineQueryParams
import com.atproto.server.CreateAccountRequest
import com.atproto.server.CreateSessionRequest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.OzoneTemplate
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorPresenter
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.login.auth.AuthInfo
import sh.christian.ozone.login.auth.Credentials
import sh.christian.ozone.login.auth.Server
import sh.christian.ozone.login.auth.ServerInfo
import sh.christian.ozone.util.toReadOnlyList
import software.ralf.app.platform.presenter.compose.ComposePresenter

/** Login state and side effects expressed as a ComposePresenter. */
@Inject
class LoginPresenter(
  private val serverRepository: sh.christian.ozone.api.ServerRepository,
  private val apiProvider: ApiProvider,
  private val errorPresenter: ErrorPresenter,
) : ComposePresenter<LoginPresenter.Input, OzoneTemplate> {
  @Composable
  override fun present(input: Input): OzoneTemplate {
    var mode by remember { mutableStateOf(LoginScreenMode.SIGN_IN) }
    var serverInfo by remember { mutableStateOf<ServerInfo?>(null) }
    var error by remember { mutableStateOf<ErrorProps?>(null) }
    val scope = rememberCoroutineScope()
    val server = serverRepository.server

    LaunchedEffect(server) {
      serverInfo = apiProvider.api.describeServer().maybeResponse()?.let { response ->
        ServerInfo(
          inviteCodeRequired = response.inviteCodeRequired ?: false,
          availableUserDomains = response.availableUserDomains.toReadOnlyList(),
          privacyPolicy = response.links?.privacyPolicy?.uri,
          termsOfService = response.links?.termsOfService?.uri,
        )
      }
    }

    val login = LoginScreen(
      mode = mode,
      onChangeMode = { mode = it },
      server = server,
      serverInfo = serverInfo,
      onChangeServer = { serverRepository.server = it },
      onExit = input.onCanceled,
      onLogin = { credentials ->
        scope.launch {
          when (val result = signIn(mode, credentials)) {
            is AtpResponse.Success -> input.onLoggedIn(result.response)
            is AtpResponse.Failure -> error = result.toErrorProps(true)
              ?: ErrorProps("Oops.", "Something bad happened.", false)
          }
        }
      },
    )

    val errorProps = error
    return if (errorProps == null) {
      OzoneTemplate.FullScreen(login)
    } else {
      val modal = errorPresenter.present(
        ErrorPresenter.Input(errorProps) { output ->
          when (output) {
            ErrorOutput.Dismiss -> error = null
            ErrorOutput.Retry -> error = null
          }
        }
      )
      OzoneTemplate.Modal(login, modal)
    }
  }

  data class Input(
    val onLoggedIn: (AuthInfo) -> Unit,
    val onCanceled: () -> Unit,
  )

  private suspend fun signIn(mode: LoginScreenMode, credentials: Credentials): AtpResponse<AuthInfo> {
    return when (mode) {
      LoginScreenMode.SIGN_UP -> apiProvider.api.createAccount(
        CreateAccountRequest(
          email = credentials.email!!,
          handle = credentials.username,
          inviteCode = credentials.inviteCode,
          password = credentials.password,
          recoveryKey = null,
        )
      ).map { AuthInfo(it.accessJwt, it.refreshJwt, it.handle, it.did) }
      LoginScreenMode.SIGN_IN -> apiProvider.api.createSession(
        CreateSessionRequest(credentials.username.handle, credentials.password)
      ).map { AuthInfo(it.accessJwt, it.refreshJwt, it.handle, it.did) }
    }
  }
}
