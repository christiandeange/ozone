package sh.christian.ozone.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

actual fun Modifier.onBackPressed(handler: () -> Unit): Modifier = composed {
  BackHandler(onBack = handler)
  Modifier
}
