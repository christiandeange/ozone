package sh.christian.ozone

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection.Companion.Next
import androidx.compose.ui.focus.FocusDirection.Companion.Previous
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sh.christian.ozone.di.AppComponent
import sh.christian.ozone.di.create
import sh.christian.ozone.store.storage
import sh.christian.ozone.ui.AppTheme
import sh.christian.ozone.ui.compose.initTypography
import java.awt.Dimension


fun main() = runBlocking {
  val storage = storage()
  val appPlacement = DesktopAppPlacement(storage)
  val component = AppComponent::class.create(storage)

  component.supervisors.forEach {
    with(it) {
      launch(SupervisorJob()) { onStart() }
    }
  }

  runBlocking {
    // Ensure that this is set up before we actually use it in the theme.
    initTypography()
  }

  application {
    val windowState = rememberWindowState(
      size = appPlacement.size,
      position = appPlacement.position,
    )

    appPlacement.size = windowState.size
    appPlacement.position = windowState.position

    Window(
      title = "",
      state = windowState,
      onCloseRequest = ::exitApplication,
    ) {
      val minDimension = with(LocalDensity.current) { 200.dp.roundToPx() }
      window.minimumSize = Dimension(minDimension, minDimension)
      window.rootPane.apply {
        putClientProperty("apple.awt.fullWindowContent", true)
        putClientProperty("apple.awt.transparentTitleBar", true)
      }

      val focusManager = LocalFocusManager.current
      Box(
        Modifier.onPreviewKeyEvent {
          if (it.key == Key.Tab && it.type == KeyDown) {
            focusManager.moveFocus(if (it.isShiftPressed) Previous else Next)
            true
          } else {
            false
          }
        }
      ) {
        AppTheme {
          App(component.appWorkflow, onExit = { exitApplication() })
        }
      }
    }
  }
}
