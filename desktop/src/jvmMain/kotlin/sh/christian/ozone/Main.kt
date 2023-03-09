package sh.christian.ozone

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection.Companion.Next
import androidx.compose.ui.focus.FocusDirection.Companion.Previous
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
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
      val focusManager = LocalFocusManager.current
      Box(
        Modifier.onPreviewKeyEvent {
          @OptIn(ExperimentalComposeUiApi::class)
          if (it.key == Key.Tab && it.type == KeyDown) {
            focusManager.moveFocus(if (it.isShiftPressed) Previous else Next)
            true
          } else {
            false
          }
        }
      ) {
        App(workflow, onExit = { exitApplication() })
      }
    }
  }
}
