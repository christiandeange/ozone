package sh.christian.ozone.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import sh.christian.ozone.ui.compose.Overlay
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.OverlayRendering
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen

class AppScreen(
  val main: ViewRendering,
  val overlay: OverlayRendering? = null,
) : ViewRendering by screen({
  main.Content()

  Overlay(
    modifier = Modifier.fillMaxSize(),
    visible = overlay != null,
    onDismiss = {
      when (val onDismiss = overlay?.onDismiss) {
        is Dismissable.DismissHandler -> onDismiss.handler()
        is Dismissable.Ignore -> Unit
        null -> Unit
      }
    },
  ) {
    overlay?.Content()
  }
})
