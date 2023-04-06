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

data class AppScreen(
  private val main: ViewRendering,
  private val overlay: OverlayRendering? = null,
) : ViewRendering by screen({
  main.Content()

  val overlayVisibility = remember { MutableTransitionState(false) }

  Overlay(
    modifier = Modifier.fillMaxSize(),
    visibleState = overlayVisibility,
    enter = overlay?.enter ?: EnterTransition.None,
    exit = overlay?.exit ?: ExitTransition.None,
    onClickOutside = {
      when (overlay?.onDismiss) {
        is DismissHandler -> overlayVisibility.targetState = false
        is Ignore -> Unit
        null -> Unit
      }
    },
  ) {
    overlay?.Content(onRequestDismiss = {
      when (overlay.onDismiss) {
        is DismissHandler -> overlayVisibility.targetState = false
        is Ignore -> Unit
      }
    })
  }

  LaunchedEffect(overlay) {
    overlayVisibility.targetState = overlay != null
  }

  LaunchedEffect(overlayVisibility.currentState) {
    if (!overlayVisibility.targetState && !overlayVisibility.currentState && overlay != null) {
      when (val onDismiss = overlay.onDismiss) {
        is DismissHandler -> onDismiss.handler()
        is Ignore -> Unit
      }
    }
  }
})
