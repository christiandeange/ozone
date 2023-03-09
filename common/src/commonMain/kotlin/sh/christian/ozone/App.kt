package sh.christian.ozone

import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import sh.christian.ozone.ui.AppTheme

@Composable
fun App() {
  var text by remember { mutableStateOf("Hello, World!") }
  val platformName = getPlatformName()

  AppTheme {
    Button(onClick = {
      text = "Hello, ${platformName}"
    }) {
      Text(text)
    }
  }
}
