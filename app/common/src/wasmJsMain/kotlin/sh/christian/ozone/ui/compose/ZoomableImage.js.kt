package sh.christian.ozone.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun ZoomableImage(
  painter: Painter,
  contentDescription: String?,
  modifier: Modifier,
  alignment: Alignment,
  contentScale: ContentScale,
  alpha: Float,
  colorFilter: ColorFilter?,
): ZoomableImageHandle {
  // Zoomable images not yet supported on JS
  Image(
    modifier = modifier,
    painter = painter,
    contentDescription = contentDescription,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
  )

  return NO_OP_HANDLE
}

private val NO_OP_HANDLE = ZoomableImageHandle(
  resetZoom = {
    // No-op
  },
)
