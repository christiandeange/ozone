package sh.christian.ozone.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale

@Composable
fun BannerImage(
  modifier: Modifier = Modifier,
  imageUrl: String?,
  contentDescription: String?,
  onClick: (() -> Unit)? = null,
  fallbackColor: Color = MaterialTheme.colorScheme.primary,
) {
  if (imageUrl != null) {
    val clickable = if (onClick != null) {
      Modifier.clickable { onClick() }
    } else {
      Modifier
    }

    when (val resource = rememberUrlPainter(imageUrl)) {
      is PainterResource.Failure,
      is PainterResource.Loading -> {
        EmptyPostImage(fallbackColor)
      }
      is PainterResource.Success -> {
        Image(
          modifier = modifier.aspectRatio(3f).then(clickable),
          painter = resource.painter,
          contentDescription = contentDescription,
          contentScale = ContentScale.Crop,
        )
      }
    }
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
