package sh.christian.ozone

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.browser.window
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.skiko.wasm.onWasmReady
import sh.christian.ozone.di.AppComponent
import sh.christian.ozone.di.create
import sh.christian.ozone.store.storage
import sh.christian.ozone.ui.AppTheme
import sh.christian.ozone.ui.compose.initTypography
import sh.christian.ozone.ui.workflow.WorkflowRendering

suspend fun main() {
  val storage = storage()
  val component = AppComponent::class.create(storage)
  val workflow = component.appWorkflow

  coroutineScope {
    component.supervisors.forEach {
      with(it) {
        launch(SupervisorJob()) { start() }
      }
    }
  }

  // Ensure that this is set up before we actually use it in the theme.
  initTypography()

  val authInfo = component.loginRepository.auth().first()

  onWasmReady {
    CanvasBasedWindow {
      val focusManager = LocalFocusManager.current
      Box(
        Modifier.onPreviewKeyEvent {
          if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
            focusManager.moveFocus(if (it.isShiftPressed) FocusDirection.Previous else FocusDirection.Next)
            true
          } else {
            false
          }
        }
      ) {
        AppTheme {
          WorkflowRendering(
            workflow = workflow,
            props = authInfo,
            onOutput = { window.close() },
            content = { it.Content() },
          )
        }
      }
    }
  }
}
