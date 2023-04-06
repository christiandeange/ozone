package sh.christian.ozone.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.apache.commons.lang3.SystemUtils

@Composable
actual fun rememberSystemInsets(): PaddingValues {
  return if (SystemUtils.IS_OS_MAC_OSX) {
    PaddingValues(top = 18.dp)
  } else {
    PaddingValues(0.dp)
  }
}
