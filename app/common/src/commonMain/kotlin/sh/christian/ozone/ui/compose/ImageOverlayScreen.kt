package sh.christian.ozone.ui.compose

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import io.kamel.core.Resource
import kotlinx.collections.immutable.persistentListOf
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.OverlayRendering
import sh.christian.ozone.ui.workflow.overlay
import sh.christian.ozone.util.ReadOnlyList

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
    val state = rememberPagerState(
      initialPage = action.selectedIndex,
      initialPageOffsetFraction = 0f,
      pageCount = action.images::size,
    )

    HorizontalPager(state = state) { page ->
      Box {
        val imageUrl = action.images[page].imageUrl
        when (val resource = urlImagePainter(imageUrl)) {
          is Resource.Failure,
          is Resource.Loading -> Unit
          is Resource.Success -> {
            val zoomableImageHandle = ZoomableImage(
              modifier = Modifier.fillMaxSize(),
              painter = resource.value,
              contentDescription = action.images[page].alt,
              contentScale = ContentScale.Inside,
              alignment = Alignment.Center,
            )

            if (state.settledPage != page) {
              LaunchedEffect(Unit) {
                zoomableImageHandle.resetZoom()
              }
            }
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
  val images: ReadOnlyList<BasicImage>,
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
