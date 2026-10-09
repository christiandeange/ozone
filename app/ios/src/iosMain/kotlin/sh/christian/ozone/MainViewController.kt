package sh.christian.ozone

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplication
import sh.christian.ozone.api.OzoneDispatchers.IO
import sh.christian.ozone.app.OzoneRuntime
import sh.christian.ozone.app.OzoneTemplateRenderer
import sh.christian.ozone.app.initOzone
import sh.christian.ozone.store.storage
import sh.christian.ozone.ui.AppTheme

lateinit var ozone: OzoneRuntime

@Suppress("unused") // Called from iOS application code.
fun initialize() {
  ozone = initOzone(CoroutineScope(IO), storage())
}

/** Call from the iOS application's final teardown hook. */
@Suppress("unused") // Called from iOS application code.
fun shutdown() {
  if (::ozone.isInitialized) ozone.close()
}

@OptIn(ExperimentalForeignApi::class)
@Suppress("unused", "FunctionName") // Called from iOS application code.
fun MainViewController() = ComposeUIViewController {
  Box {
    AppTheme {
      val template by ozone.templates.collectAsState()
      OzoneTemplateRenderer().renderCompose(template)
    }
  }
}
