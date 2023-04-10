package sh.christian.ozone.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import io.kamel.image.KamelImage

@Composable
fun AvatarImage(
  modifier: Modifier = Modifier,
  avatarUrl: String?,
  contentDescription: String?,
  onClick: (() -> Unit)? = null,
  fallbackColor: Color = Color.Transparent,
) {
  val clickableModifier = if (onClick != null) {
    Modifier.clickable { onClick() }
  } else {
    Modifier
  }

  if (avatarUrl != null) {
    KamelImage(
      modifier = modifier
        .clip(CircleShape)
        .then(clickableModifier),
      resource = rememberUrlPainter(avatarUrl),
      contentDescription = contentDescription,
      onLoading = { EmptyAvatar(fallbackColor) },
      onFailure = { EmptyAvatar(fallbackColor) },
      contentScale = ContentScale.Crop,
    )
  } else {
    EmptyAvatar(
      modifier = modifier,
      fallbackColor = fallbackColor,
      onClick = onClick,
    )
  }
}

@Composable
private fun EmptyAvatar(
  fallbackColor: Color,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) {
  val clickableModifier = if (onClick != null) {
    Modifier.clickable { onClick() }
  } else {
    Modifier
  }
  Box(
    modifier = modifier
      .clip(CircleShape)
      .background(fallbackColor)
      .then(clickableModifier)
  )
}
