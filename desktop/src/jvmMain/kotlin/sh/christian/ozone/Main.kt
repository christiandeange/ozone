package sh.christian.ozone

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.login.LoginWorkflow


fun main() {
  val workflow = AppWorkflow(
    loginWorkflow = LoginWorkflow()
  )

  application {
    Window(
      title = "Ozone",
      onCloseRequest = ::exitApplication,
    ) {
      App(workflow, onExit = { exitApplication() })
    }
  }
}
