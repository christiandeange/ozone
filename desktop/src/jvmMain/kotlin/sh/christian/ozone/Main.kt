package sh.christian.ozone

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import sh.christian.ozone.common.App


fun main() = application {
  Window(
    title = "Ozone",
    onCloseRequest = ::exitApplication,
  ) {
    App()
  }
}
