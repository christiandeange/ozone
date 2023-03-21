package sh.christian.ozone.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun Modifier.onBackPressed(handler: () -> Unit): Modifier
