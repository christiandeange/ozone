package sh.christian.ozone.util

import androidx.compose.runtime.Composable
import sh.christian.ozone.model.Moment

@Composable
expect fun Moment.formatDate(): String

@Composable
expect fun Moment.formatTime(): String
