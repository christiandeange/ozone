package sh.christian.ozone.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import sh.christian.ozone.ui.compose.Overlay
import sh.christian.ozone.ui.workflow.Dismissable.DismissHandler
import sh.christian.ozone.ui.workflow.Dismissable.Ignore
import sh.christian.ozone.ui.workflow.OverlayRendering
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen

class AppScreen(
  val main: ViewRendering,
  val overlay: OverlayRendering? = null,
) : ViewRendering by screen({
  main.Content()
  val visibleState = remember { MutableTransitionState(false) }

  Overlay(
    modifier = Modifier.fillMaxSize(),
    visibleState = visibleState,
    enter = overlay?.enter ?: EnterTransition.None,
    exit = overlay?.exit ?: ExitTransition.None,
    onClickOutside = {
      when (overlay?.onDismiss) {
        is DismissHandler -> visibleState.targetState = false
        is Ignore -> Unit
        null -> Unit
      }
    },
  ) {
    overlay?.Content(onRequestDismiss = {
      when (overlay.onDismiss) {
        is DismissHandler -> visibleState.targetState = false
        is Ignore -> Unit
      }
    })
  }

  LaunchedEffect(overlay, visibleState) {
    visibleState.targetState = overlay != null

    if (!visibleState.targetState && !visibleState.currentState && overlay != null) {
      when (val onDismiss = overlay.onDismiss) {
        is DismissHandler -> onDismiss.handler()
        is Ignore -> Unit
      }
    }
  }
})
