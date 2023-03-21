package sh.christian.ozone.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

@Composable
actual fun Modifier.onBackPressed(handler: () -> Unit): Modifier = composed {
  // No-op
  Modifier
}
