package sh.christian.ozone.timeline.components.feature

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.kamel.image.lazyPainterResource
import sh.christian.ozone.model.TimelinePostFeature.ImagesFeature
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.ui.compose.PostImage

@Composable
internal fun PostImages(
  feature: ImagesFeature,
  onOpenImage: (OpenImageAction) -> Unit,
) {
  Row(horizontalArrangement = spacedBy(8.dp)) {
    feature.images.forEach { image ->
      // Preload the full-size image into the cache.
      lazyPainterResource(image.fullsize)

      PostImage(
        modifier = Modifier
          .weight(1f)
          .aspectRatio(1f),
        imageUrl = image.thumb,
        contentDescription = image.alt,
        onClick = {
          onOpenImage(OpenImageAction(image.fullsize, image.alt))
        },
        fallbackColor = MaterialTheme.colorScheme.outline,
      )
    }
  }
}
