package sh.christian.ozone.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
actual fun rememberSystemInsets(): PaddingValues {
  return PaddingValues(0.dp)
}
