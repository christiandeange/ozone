package sh.christian.ozone.ui.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OverImageIconButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Surface(
    modifier = modifier
      .padding(8.dp)
      .size(32.dp),
    shape = CircleShape,
    color = Color.Black.copy(alpha = 0.5f),
  ) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
      IconButton(onClick = onClick) {
        content()
      }
    }
  }
}