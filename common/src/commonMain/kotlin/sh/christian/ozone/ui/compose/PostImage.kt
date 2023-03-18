package sh.christian.ozone.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource

@Composable
fun PostImage(
  modifier: Modifier = Modifier,
  imageUrl: String?,
  contentDescription: String?,
  onClick: () -> Unit,
  fallbackColor: Color = Color.Transparent,
) {
  if (imageUrl != null) {
    KamelImage(
      modifier = modifier
        .clip(MaterialTheme.shapes.large)
        .clickable { onClick() },
      resource = lazyPainterResource(imageUrl),
      contentDescription = contentDescription,
      onLoading = { EmptyAvatar(fallbackColor) },
      onFailure = { EmptyAvatar(fallbackColor) },
      contentScale = ContentScale.FillWidth,
    )
  } else {
    EmptyAvatar(fallbackColor, modifier, onClick)
  }
}

@Composable
private fun EmptyAvatar(
  fallbackColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  Box(
    modifier = modifier
      .clickable { onClick() }
      .clip(MaterialTheme.shapes.large)
      .background(fallbackColor)
  )
}
