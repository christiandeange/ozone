package sh.christian.ozone

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.coroutineScope
import org.jetbrains.skiko.wasm.onWasmReady
import sh.christian.ozone.app.OzoneTemplateRenderer
import sh.christian.ozone.app.initOzone
import sh.christian.ozone.store.storage
import sh.christian.ozone.ui.AppTheme

suspend fun main() {
  val ozone = coroutineScope { initOzone(this, storage()) }

  onWasmReady {
    ComposeViewport(document.body!!) {
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
          val template by ozone.templates.collectAsState()
          OzoneTemplateRenderer().renderCompose(template)
        }
      }
    }
  }
}
