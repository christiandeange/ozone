package sh.christian.ozone.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sh.christian.ozone.ui.icons.AlternateEmail
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen

@OptIn(ExperimentalMaterial3Api::class)
class LoginScreen(
  private val onCancel: () -> Unit,
  private val onLogin: (Credentials) -> Unit,
) : ViewRendering by screen({
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
        placeholder = { Text("Username or email address") },
        shape = MaterialTheme.shapes.large,
      )

      Spacer(Modifier.height(8.dp))

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
        placeholder = { Text("Password") },
        shape = MaterialTheme.shapes.large,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
      )

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
})
