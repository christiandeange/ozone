package sh.christian.ozone.timeline.components.feature

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.christian.ozone.model.TimelinePostFeature.ImagesFeature
import sh.christian.ozone.ui.compose.BasicImage
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.ui.compose.PostImage
import sh.christian.ozone.ui.compose.rememberUrlPainter
import sh.christian.ozone.util.mapImmutable

@Composable
internal fun PostImages(
  feature: ImagesFeature,
  onOpenImage: (OpenImageAction) -> Unit,
) {
  val fullSizeImages = remember(feature.images) {
    feature.images.mapImmutable {
      BasicImage(it.fullsize, it.alt)
    }
  }

  Row(horizontalArrangement = spacedBy(8.dp)) {
    val modifier = if (feature.images.size == 1) {
      Modifier
    } else {
      Modifier
        .weight(1f)
        .aspectRatio(1f)
    }

    feature.images.forEachIndexed { i, image ->
      // Preload the full-size image into the cache.
      rememberUrlPainter(image.fullsize)

      PostImage(
        modifier = modifier,
        imageUrl = image.thumb,
        contentDescription = image.alt,
        onClick = {
          onOpenImage(OpenImageAction(fullSizeImages, i))
        },
        fallbackColor = MaterialTheme.colorScheme.outline,
      )
    }
  }
}
