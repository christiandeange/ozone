package sh.christian.ozone.ui.compose

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.OverlayRendering
import sh.christian.ozone.ui.workflow.overlay

class ImageOverlayScreen(
  onDismiss: Dismissable.DismissHandler,
  private val action: OpenImageAction,
) : OverlayRendering by overlay(onDismiss, { onRequestDismiss ->
  Surface(
    modifier = Modifier
      .fillMaxSize(),
    color = Color.Black.copy(alpha = 0.9f),
  ) {
    Box {
      var scale by remember { mutableStateOf(1f) }
      var offsetX by remember { mutableStateOf(0f) }
      var offsetY by remember { mutableStateOf(0f) }
      var size by remember { mutableStateOf(IntSize.Zero) }

      val maxX by derivedStateOf { (size.width * (scale - 1)) / 2 }
      val maxY by derivedStateOf { (size.height * (scale - 1)) / 2 }

      KamelImage(
        modifier = Modifier
          .onBackPressed { onRequestDismiss() }
          .align(Alignment.Center)
          .onSizeChanged { size = it }
          .pointerInput(Unit) {
            detectTransformGestures(panZoomLock = true) { _, pan, zoom, _ ->
              scale = (scale * zoom).coerceIn(1f, 4f)
              offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
              offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
            }
          }
          .graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationX = offsetX
            translationY = offsetY
          },
        resource = lazyPainterResource(action.imageUrl),
        contentDescription = action.alt,
        contentScale = ContentScale.Fit,
      )

      Surface(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(8.dp)
          .size(32.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.5f),
      ) {
        IconButton(onClick = onRequestDismiss) {
          Icon(
            painter = rememberVectorPainter(Icons.Default.Close),
            contentDescription = "Close",
            tint = Color.White,
          )
        }
      }
    }
  }
}) {
  override val enter: EnterTransition = fadeIn()
  override val exit: ExitTransition = fadeOut()
}

data class OpenImageAction(
  val imageUrl: String,
  val alt: String?,
)
