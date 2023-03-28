package sh.christian.ozone.ui.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color

fun Modifier.foreground(color: Color): Modifier = drawWithContent {
  drawContent()
  drawRect(color)
}
