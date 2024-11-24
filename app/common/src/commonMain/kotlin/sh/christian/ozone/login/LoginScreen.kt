package sh.christian.ozone.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.AnimationConstants.DefaultDurationMillis
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation.Companion.None
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import sh.christian.ozone.api.Handle
import sh.christian.ozone.login.LoginScreenMode.SIGN_IN
import sh.christian.ozone.login.LoginScreenMode.SIGN_UP
import sh.christian.ozone.login.auth.Credentials
import sh.christian.ozone.login.auth.Server
import sh.christian.ozone.login.auth.Server.BlueskySocial
import sh.christian.ozone.login.auth.Server.CustomServer
import sh.christian.ozone.login.auth.ServerInfo
import sh.christian.ozone.ui.compose.Overlay
import sh.christian.ozone.ui.compose.StablePainter
import sh.christian.ozone.ui.compose.SystemInsets
import sh.christian.ozone.ui.compose.autofill
import sh.christian.ozone.ui.compose.heroFont
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.compose.rememberSystemInsets
import sh.christian.ozone.ui.compose.stable
import sh.christian.ozone.ui.icons.AlternateEmail
import sh.christian.ozone.ui.icons.LocalActivity
import sh.christian.ozone.ui.icons.Visibility
import sh.christian.ozone.ui.icons.VisibilityOff
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen
import sh.christian.ozone.util.ReadOnlyList

class LoginScreen(
  private val mode: LoginScreenMode,
  private val onChangeMode: (LoginScreenMode) -> Unit,
  private val server: Server,
  private val serverInfo: ServerInfo?,
  private val onChangeServer: (Server) -> Unit,
  private val onExit: () -> Unit,
  private val onLogin: (Credentials) -> Unit,
) : ViewRendering by screen({
  val expandBottomSheet = remember { MutableTransitionState(false) }

  Column(
    Modifier
      .fillMaxSize()
      .background(rememberLoginGradientBrush())
      .padding(rememberSystemInsets())
      .onBackPressed(onExit)
  ) {
    val emailField = remember(mode) { mutableStateOf("") }
    val usernameField = remember(mode) { mutableStateOf("") }
    val passwordField = remember(mode) { mutableStateOf("") }
    val inviteCodeField = remember(mode) { mutableStateOf("") }

    val email by emailField
    val username by usernameField
    val password by passwordField
    val inviteCode by inviteCodeField

    LargeTopAppBar(
      title = {
        Text(
          text = "Welcome to Ozone.",
          style = MaterialTheme.typography.titleLarge.copy(fontFamily = heroFont()),
        )
      },
      actions = {
        TextButton(onClick = {
          onChangeMode(
            when (mode) {
              SIGN_UP -> SIGN_IN
              SIGN_IN -> SIGN_UP
            }
          )
        }) {
          Text(
            when (mode) {
              SIGN_UP -> "Have an account?"
              SIGN_IN -> "Need an account?"
            }
          )
        }
      },
      colors = TopAppBarDefaults.mediumTopAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
      ),
      windowInsets = WindowInsets(0.dp),
    )

    Spacer(Modifier.weight(1f))

    Column(
      modifier = Modifier
        .padding(32.dp)
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      val showInviteCodeField = mode == SIGN_UP && serverInfo?.inviteCodeRequired == true

      val allFieldsCompleted = username.isNotEmpty() && password.isNotEmpty() && when (mode) {
        SIGN_UP -> email.isNotEmpty() && (!showInviteCodeField || inviteCode.isNotEmpty())
        SIGN_IN -> true
      }

      val animationDistance = with(LocalDensity.current) { 32.dp.roundToPx() }

      val fadeSpec1 = tween<Float>(delayMillis = 0)
      val slideSpec1 = tween<IntOffset>(delayMillis = 0)
      AnimatedVisibility(
        visible = showInviteCodeField,
        enter = fadeIn(fadeSpec1) + slideInHorizontally(slideSpec1) { -animationDistance },
        exit = fadeOut(fadeSpec1) + slideOutHorizontally(slideSpec1) { animationDistance },
      ) {
        Column {
          InviteCodeField(
            modifier = Modifier.fillMaxWidth(),
            inviteCode = inviteCodeField,
          )

          Spacer(Modifier.height(8.dp))
        }
      }

      val fadeSpec2 = tween<Float>(delayMillis = DefaultDurationMillis / 3)
      val slideSpec2 = tween<IntOffset>(delayMillis = DefaultDurationMillis / 3)
      AnimatedVisibility(
        visible = mode == SIGN_UP,
        enter = fadeIn(fadeSpec2) + slideInHorizontally(slideSpec2) { -animationDistance },
        exit = fadeOut(fadeSpec2) + slideOutHorizontally(slideSpec2) { animationDistance },
      ) {
        Column {
          BasicInputField(
            modifier = Modifier.fillMaxWidth(),
            label = "Email Address",
            icon = rememberVectorPainter(Icons.Default.Email).stable,
            field = emailField,
            autofillTypes = persistentListOf(AutofillType.EmailAddress),
          )

          Spacer(Modifier.height(8.dp))
        }
      }

      BasicInputField(
        modifier = Modifier.fillMaxWidth(),
        label = "Username",
        icon = rememberVectorPainter(Icons.Default.AlternateEmail).stable,
        field = usernameField,
        autofillTypes = persistentListOf(AutofillType.Username),
      )

      Spacer(Modifier.height(8.dp))

      PasswordField(
        modifier = Modifier.fillMaxWidth(),
        password = passwordField,
        onKeyboardAction = {
          if (allFieldsCompleted) {
            onLogin(Credentials(email, Handle(username), password, inviteCode))
          }
        },
      )

      Spacer(Modifier.height(8.dp))

      ServerSelector(
        modifier = Modifier.align(Alignment.End),
        server = server,
        onClick = { expandBottomSheet.targetState = true },
      )

      Spacer(Modifier.height(32.dp))

      SubmitButton(
        modifier = Modifier.fillMaxWidth(),
        mode = mode,
        enabled = allFieldsCompleted,
        onClick = { onLogin(Credentials(email, Handle(username), password, inviteCode)) },
      )
    }
  }

  ServerSelectorOverlay(
    visibility = expandBottomSheet,
    server = server,
    onChangeServer = onChangeServer,
  )
})

@Composable
private fun InviteCodeField(
  modifier: Modifier = Modifier,
  inviteCode: MutableState<String>,
) {
  OutlinedTextField(
    modifier = modifier,
    value = inviteCode.value,
    onValueChange = { inviteCode.value = it },
    leadingIcon = {
      Icon(
        painter = rememberVectorPainter(Icons.Default.LocalActivity),
        contentDescription = "Invite Code",
      )
    },
    singleLine = true,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    placeholder = { Text("Invite Code") },
    shape = MaterialTheme.shapes.large,
  )
}

@Composable
private fun BasicInputField(
  modifier: Modifier = Modifier,
  label: String,
  icon: StablePainter,
  field: MutableState<String>,
  autofillTypes: ReadOnlyList<AutofillType>,
) {
  OutlinedTextField(
    modifier = modifier.autofill(
      autofillTypes = autofillTypes,
      onFill = { field.value = it },
    ),
    value = field.value,
    onValueChange = { field.value = it },
    leadingIcon = {
      Icon(
        painter = icon.painter,
        contentDescription = label,
      )
    },
    singleLine = true,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    placeholder = { Text(label) },
    shape = MaterialTheme.shapes.large,
  )
}

@Composable
private fun PasswordField(
  modifier: Modifier = Modifier,
  password: MutableState<String>,
  onKeyboardAction: () -> Unit,
) {
  var showPassword by remember { mutableStateOf(false) }
  OutlinedTextField(
    modifier = modifier.autofill(
      autofillTypes = listOf(AutofillType.Password),
      onFill = { password.value = it },
    ),
    value = password.value,
    onValueChange = { password.value = it },
    leadingIcon = {
      Icon(
        painter = rememberVectorPainter(Icons.Default.Lock),
        contentDescription = "Password",
      )
    },
    trailingIcon = {
      IconButton(onClick = { showPassword = !showPassword }) {
        Icon(
          painter = rememberVectorPainter(
            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
          ),
          contentDescription = "Toggle Password Visibility",
        )
      }
    },
    singleLine = true,
    placeholder = { Text("Password") },
    shape = MaterialTheme.shapes.large,
    visualTransformation = if (showPassword) None else PasswordVisualTransformation(),
    keyboardOptions = KeyboardOptions(
      keyboardType = if (showPassword) KeyboardType.Text else KeyboardType.Password,
      imeAction = ImeAction.Send,
    ),
    keyboardActions = KeyboardActions {
      onKeyboardAction()
    },
  )
}

@Composable
private fun SubmitButton(
  modifier: Modifier = Modifier,
  mode: LoginScreenMode,
  enabled: Boolean,
  onClick: () -> Unit,
) {
  Crossfade(targetState = mode) { targetMode ->
    Button(
      modifier = modifier,
      enabled = enabled,
      onClick = onClick,
    ) {
      Text(
        text = when (targetMode) {
          SIGN_UP -> "Sign Up"
          SIGN_IN -> "Sign In"
        },
        style = MaterialTheme.typography.titleMedium,
      )
    }
  }
}

@Composable
private fun ServerSelector(
  modifier: Modifier = Modifier,
  server: Server,
  onClick: () -> Unit,
) {
  Button(
    modifier = modifier,
    contentPadding = PaddingValues(16.dp, 8.dp),
    onClick = onClick,
  ) {
    Icon(
      modifier = Modifier.size(18.dp),
      painter = rememberVectorPainter(Icons.Default.Home),
      contentDescription = "Server",
    )

    Text(
      modifier = Modifier.padding(start = 8.dp),
      text = when (server) {
        is BlueskySocial -> "Bluesky Social"
        is CustomServer -> server.host
      },
      style = MaterialTheme.typography.bodySmall.copy(
        color = LocalContentColor.current,
      ),
    )
  }
}

@Composable
private fun ServerSelectorOverlay(
  modifier: Modifier = Modifier,
  visibility: MutableTransitionState<Boolean>,
  server: Server,
  onChangeServer: (Server) -> Unit,
) {
  Overlay(
    modifier = modifier.fillMaxSize(),
    visibleState = visibility,
    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
    onClickOutside = { visibility.targetState = false },
  ) {
    SystemInsets {
      Surface(shadowElevation = 16.dp) {
        Column(
          modifier = Modifier
            .padding(32.dp)
            .fillMaxWidth(),
          verticalArrangement = spacedBy(8.dp),
        ) {
          Button(
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp),
            onClick = {
              onChangeServer(BlueskySocial)
              visibility.targetState = false
            },
            shape = MaterialTheme.shapes.large,
          ) {
            Text("Bluesky Social")
          }

          var customServer by remember {
            mutableStateOf(
              when (server) {
                is BlueskySocial -> ""
                is CustomServer -> server.host
              }
            )
          }
          OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = customServer,
            onValueChange = { customServer = it },
            placeholder = { Text("Custom Server") },
            trailingIcon = {
              IconButton(
                enabled = customServer.isNotBlank(),
                onClick = {
                  onChangeServer(CustomServer(customServer))
                  visibility.targetState = false
                },
              ) {
                Icon(
                  painter = rememberVectorPainter(Icons.Default.CheckCircle),
                  contentDescription = "Save Custom Server",
                )
              }
            },
            shape = MaterialTheme.shapes.large,
          )
        }
      }
    }
  }
}

enum class LoginScreenMode {
  SIGN_UP,
  SIGN_IN,
}
