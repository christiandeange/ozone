package sh.christian.ozone.ui.workflow

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable

class CompositeViewRendering(
  val renderings: List<ViewRendering>,
) : ViewRendering {
  @Composable
  override fun Content() {
    Box {
      renderings.forEach { it.Content() }
    }
  }
}

operator fun ViewRendering.plus(other: ViewRendering): ViewRendering = when {
  this is CompositeViewRendering && other is CompositeViewRendering ->
    CompositeViewRendering(renderings + other.renderings)
  this is CompositeViewRendering -> CompositeViewRendering(renderings + other)
  other is CompositeViewRendering -> CompositeViewRendering(listOf(this) + other.renderings)
  else -> CompositeViewRendering(listOf(this, other))
}
