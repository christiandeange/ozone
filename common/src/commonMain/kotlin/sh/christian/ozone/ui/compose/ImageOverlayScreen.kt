package sh.christian.ozone.ui.compose

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.OverlayRendering
import sh.christian.ozone.ui.workflow.overlay

@OptIn(ExperimentalFoundationApi::class)
class ImageOverlayScreen(
  onDismiss: Dismissable.DismissHandler,
  private val action: OpenImageAction,
) : OverlayRendering by overlay(onDismiss, { onRequestDismiss ->
  Surface(
    modifier = Modifier
      .fillMaxSize()
      .onBackPressed { onRequestDismiss() },
    color = Color.Black.copy(alpha = 0.8f),
  ) {
    val state = rememberPagerState(initialPage = action.selectedIndex)

    HorizontalPager(
      pageCount = action.images.size,
      state = state,
    ) { page ->
      Box {
        var scale by remember { mutableStateOf(1f) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        val maxX by remember { derivedStateOf { (size.width * (scale - 1)) / 2 } }
        val maxY by remember { derivedStateOf { (size.height * (scale - 1)) / 2 } }

        val url = action.images[page].imageUrl
        when (val resource = rememberUrlPainter(url)) {
          is PainterResource.Failure,
          is PainterResource.Loading -> Unit
          is PainterResource.Success -> {
            Image(
              modifier = Modifier
                .fillMaxSize()
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
              painter = resource.painter,
              contentDescription = action.images[page].alt,
              contentScale = ContentScale.Fit,
            )
          }
        }
      }
    }

    SystemInsets {
      OverImageIconButton(
        modifier = Modifier.align(Alignment.TopStart),
        onClick = onRequestDismiss,
      ) {
        Icon(
          painter = rememberVectorPainter(Icons.Default.Close),
          contentDescription = "Close",
        )
      }
    }
  }
}) {
  override val enter: EnterTransition = fadeIn()
  override val exit: ExitTransition = fadeOut()
}

data class OpenImageAction(
  val images: ImmutableList<BasicImage>,
  val selectedIndex: Int,
) {
  init {
    check(images.isNotEmpty()) { "List of images is empty" }
  }

  constructor(image: BasicImage) : this(persistentListOf(image), 0)
}

data class BasicImage(
  val imageUrl: String,
  val alt: String?,
)
