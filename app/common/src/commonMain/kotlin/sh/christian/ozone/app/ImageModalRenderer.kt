package sh.christian.ozone.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import io.kamel.core.Resource
import sh.christian.ozone.ui.compose.OverImageIconButton
import sh.christian.ozone.ui.compose.SystemInsets
import sh.christian.ozone.ui.compose.ZoomableImage
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.compose.urlImagePainter
import software.ralf.app.platform.inject.ContributesRenderer
import software.ralf.app.platform.renderer.ComposeRenderer

/** Renders the image gallery independently of the presenter that opened it. */
@OptIn(ExperimentalFoundationApi::class)
@ContributesRenderer
class ImageModalRenderer : ComposeRenderer<ImageModalModel>() {
  @Composable
  override fun Compose(model: ImageModalModel, modifier: Modifier) {
    Surface(
      modifier = modifier
        .fillMaxSize()
        .onBackPressed { model.onRequestDismiss?.invoke() },
      color = Color.Black.copy(alpha = 0.8f),
    ) {
      val state = rememberPagerState(
        initialPage = model.action.selectedIndex,
        initialPageOffsetFraction = 0f,
        pageCount = model.action.images::size,
      )

      HorizontalPager(state = state) { page ->
        Box {
          val image = model.action.images[page]
          when (val resource = urlImagePainter(image.imageUrl)) {
            is Resource.Failure,
            is Resource.Loading -> Unit
            is Resource.Success -> {
              val zoomableImage = ZoomableImage(
                modifier = Modifier.fillMaxSize(),
                painter = resource.value,
                contentDescription = image.alt,
                contentScale = ContentScale.Inside,
                alignment = Alignment.Center,
              )
              if (state.settledPage != page) {
                LaunchedEffect(page) { zoomableImage.resetZoom() }
              }
            }
          }
        }
      }

      SystemInsets {
        OverImageIconButton(
          modifier = Modifier.align(Alignment.TopStart),
          onClick = { model.onRequestDismiss?.invoke() },
        ) {
          Icon(
            painter = rememberVectorPainter(Icons.Default.Close),
            contentDescription = "Close",
          )
        }
      }
    }
  }
}
