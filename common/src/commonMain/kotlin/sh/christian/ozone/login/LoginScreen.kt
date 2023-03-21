package sh.christian.ozone.login

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation.Companion.None
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sh.christian.ozone.login.auth.Credentials
import sh.christian.ozone.login.auth.Server
import sh.christian.ozone.login.auth.Server.BlueskySocial
import sh.christian.ozone.login.auth.Server.CustomServer
import sh.christian.ozone.ui.compose.Overlay
import sh.christian.ozone.ui.icons.AlternateEmail
import sh.christian.ozone.ui.icons.Visibility
import sh.christian.ozone.ui.icons.VisibilityOff
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen

@OptIn(ExperimentalMaterial3Api::class)
class LoginScreen(
  private val server: Server,
  private val onChangeServer: (Server) -> Unit,
  private val onCancel: () -> Unit,
  private val onLogin: (Credentials) -> Unit,
) : ViewRendering by screen({
  var expandBottomSheet = remember { MutableTransitionState(false) }

  Column(Modifier.fillMaxSize()) {
    var username: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }

    Text(
      modifier = Modifier
        .padding(32.dp)
        .align(Alignment.CenterHorizontally),
      text = "Welcome to Ozone.",
      textAlign = TextAlign.Center,
    )

    Spacer(Modifier.weight(1f))

    Column(
      modifier = Modifier
        .padding(32.dp)
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = username,
        onValueChange = { username = it },
        leadingIcon = {
          Icon(
            painter = rememberVectorPainter(Icons.Default.AlternateEmail),
            contentDescription = "Username",
          )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        placeholder = { Text("Username or email address") },
        shape = MaterialTheme.shapes.large,
      )

      Spacer(Modifier.height(8.dp))

      var showPassword by remember { mutableStateOf(false) }
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = password,
        onValueChange = { password = it },
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
          if (username.isNotEmpty() && password.isNotEmpty()) {
            onLogin(Credentials(username, password))
          }
        },
      )

      Spacer(Modifier.height(8.dp))

      FilledTonalButton(
        modifier = Modifier.align(Alignment.End),
        contentPadding = PaddingValues(16.dp, 8.dp),
        onClick = { expandBottomSheet.targetState = true },
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
          style = MaterialTheme.typography.bodySmall,
        )
      }

      Spacer(Modifier.height(32.dp))

      Button(
        modifier = Modifier
          .fillMaxWidth(),
        enabled = username.isNotEmpty() && password.isNotEmpty(),
        onClick = { onLogin(Credentials(username, password)) },
      ) {
        Text(
          text = "Sign In",
          style = MaterialTheme.typography.titleMedium,
        )
      }
    }
  }

  Overlay(
    modifier = Modifier.fillMaxSize(),
    visibleState = expandBottomSheet,
    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
    onClickOutside = { expandBottomSheet.targetState = false },
  ) {
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
            expandBottomSheet.targetState = false
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
                expandBottomSheet.targetState = false
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
})
