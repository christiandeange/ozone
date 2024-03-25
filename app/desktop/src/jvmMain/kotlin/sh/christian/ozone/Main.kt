package sh.christian.ozone

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.DisposableEffect
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
import kotlinx.coroutines.runBlocking
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.app.initWorkflow
import sh.christian.ozone.store.storage
import sh.christian.ozone.ui.AppTheme
import sh.christian.ozone.ui.workflow.WorkflowRendering
import java.awt.Dimension
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener

fun main() = runBlocking {
  val storage = storage()
  val appPlacement = DesktopAppPlacement(storage)
  val workflow: AppWorkflow = initWorkflow(this, storage)

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

      DisposableEffect(Unit) {
        val componentListener = object : ComponentListener {
          override fun componentResized(e: ComponentEvent) = Unit
          override fun componentHidden(e: ComponentEvent) = Unit
          override fun componentMoved(e: ComponentEvent) = Unit
          override fun componentShown(e: ComponentEvent) {
            window.minimumSize = Dimension(minDimension, minDimension)
          }
        }

        window.addComponentListener(componentListener)

        onDispose {
          window.removeComponentListener(componentListener)
        }
      }

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
          WorkflowRendering(
            workflow = workflow,
            onOutput = { exitApplication() },
            content = { it.Content() },
          )
        }
      }
    }
  }
}
