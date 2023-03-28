package sh.christian.ozone.util

import androidx.compose.ui.graphics.Color

fun String.color(): Color {
  return Color(0xFF000000 or (hashCode().toLong() and 0x00FFFFFF))
}
