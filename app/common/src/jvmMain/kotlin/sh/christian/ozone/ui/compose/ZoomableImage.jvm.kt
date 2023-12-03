package sh.christian.ozone.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

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
  val zoomableState = rememberZoomableState()

  Image(
    modifier = modifier.zoomable(zoomableState),
    painter = painter,
    contentDescription = contentDescription,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
  )

  LaunchedEffect(painter) {
    zoomableState.setContentLocation(
      ZoomableContentLocation.scaledInsideAndCenterAligned(painter.intrinsicSize)
    )
  }

  return remember(zoomableState) {
    ZoomableImageHandle(resetZoom = {
      zoomableState.resetZoom(withAnimation = false)
    })
  }
}
