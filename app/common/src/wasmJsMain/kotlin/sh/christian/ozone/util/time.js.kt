package sh.christian.ozone.util

import androidx.compose.runtime.Composable
import sh.christian.ozone.model.Moment

@Composable
actual fun Moment.formatDate(): String {
  return instant.toString().substringBefore('T')
}

@Composable
actual fun Moment.formatTime(): String {
  return instant.toString().substringAfter('T').substringBefore('Z')
}
