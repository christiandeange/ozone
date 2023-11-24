package sh.christian.ozone.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import io.kamel.core.Resource
import io.kamel.image.asyncPainterResource

@Composable
fun urlImagePainter(url: String): Resource<Painter> {
  return asyncPainterResource(url)
}
