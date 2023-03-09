package sh.christian.ozone.ui.workflow

import androidx.compose.runtime.Composable

interface ViewRendering {
  @Composable
  fun Content()
}

fun screen(content: @Composable () -> Unit): ViewRendering = object : ViewRendering {
  @Composable
  override fun Content() = content()
}
