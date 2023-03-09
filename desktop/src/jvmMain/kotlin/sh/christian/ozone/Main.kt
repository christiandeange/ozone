package sh.christian.ozone

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.login.LoginWorkflow
import sh.christian.ozone.store.storage


fun main() {
  val storage = storage()
  val appPlacement = DesktopAppPlacement(storage)
  val workflow = AppWorkflow(
    loginWorkflow = LoginWorkflow(
      loginRepository = LoginRepository(storage),
    )
  )

  application {
    val windowState = rememberWindowState(
      size = appPlacement.size,
      position = appPlacement.position,
    )

    appPlacement.size = windowState.size
    appPlacement.position = windowState.position

    Window(
      title = "Ozone",
      state = windowState,
      onCloseRequest = ::exitApplication,
    ) {
      App(workflow, onExit = { exitApplication() })
    }
  }
}
