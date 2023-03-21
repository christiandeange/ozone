package sh.christian.ozone.ui.workflow

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable

interface OverlayRendering : ViewRendering {
  val onDismiss: Dismissable

  val enter: EnterTransition get() = fadeIn() + slideInVertically(initialOffsetY = { it })
  val exit: ExitTransition get() = fadeOut() + slideOutVertically(targetOffsetY = { it })

  @Composable
  fun Content(onRequestDismiss: () -> Unit)
}

sealed interface Dismissable {
  object Ignore : Dismissable
  data class DismissHandler(val handler: () -> Unit) : Dismissable
}

fun overlay(
  onDismiss: Dismissable,
  content: @Composable (onRequestDismiss: () -> Unit) -> Unit,
): OverlayRendering = object : OverlayRendering {
  override val onDismiss: Dismissable = onDismiss
  override var enter: EnterTransition = super.enter
  override var exit: ExitTransition = super.exit

  @Composable
  override fun Content(onRequestDismiss: () -> Unit) = content(onRequestDismiss)

  @Composable
  override fun Content() = error("")
}