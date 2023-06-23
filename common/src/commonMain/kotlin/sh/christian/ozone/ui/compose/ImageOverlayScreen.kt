package sh.christian.ozone.ui.compose

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
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
        val zoomableState = rememberZoomableState()

        val url = action.images[page].imageUrl
        when (val resource = rememberUrlPainter(url)) {
          is PainterResource.Failure,
          is PainterResource.Loading -> Unit
          is PainterResource.Success -> {
            val painter = resource.painter

            Image(
              modifier = Modifier
                .fillMaxSize()
                .zoomable(zoomableState),
              painter = painter,
              contentDescription = action.images[page].alt,
              contentScale = ContentScale.Inside,
              alignment = Alignment.Center,
            )

            LaunchedEffect(painter) {
              zoomableState.setContentLocation(
                ZoomableContentLocation.scaledInsideAndCenterAligned(painter.intrinsicSize)
              )
            }

            if (state.settledPage != page) {
              LaunchedEffect(Unit) {
                zoomableState.resetZoom(withAnimation = false)
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
