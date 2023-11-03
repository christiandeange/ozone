package sh.christian.ozone.ui.compose

import androidx.compose.ui.Modifier

expect fun Modifier.onBackPressed(handler: () -> Unit): Modifier
