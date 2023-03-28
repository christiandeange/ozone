package sh.christian.ozone.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource

@Composable
fun BannerImage(
  modifier: Modifier = Modifier,
  imageUrl: String?,
  contentDescription: String?,
  onClick: (() -> Unit)? = null,
  fallbackColor: Color = MaterialTheme.colorScheme.primary,
) {
  if (imageUrl != null) {
    val resource = lazyPainterResource(imageUrl)
    val clickable = if (resource is Resource.Success && onClick != null) {
      Modifier.clickable { onClick() }
    } else {
      Modifier
    }

    KamelImage(
      modifier = modifier.aspectRatio(3f).then(clickable),
      resource = resource,
      contentDescription = contentDescription,
      onLoading = { EmptyPostImage(fallbackColor) },
      onFailure = { EmptyPostImage(fallbackColor) },
      contentScale = ContentScale.Crop,
    )
  } else {
    EmptyPostImage(fallbackColor, modifier)
  }
}

@Composable
private fun EmptyPostImage(
  fallbackColor: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .aspectRatio(3f)
      .background(fallbackColor)
  )
}
