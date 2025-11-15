package sh.christian.ozone.util

import androidx.compose.runtime.Composable
import sh.christian.ozone.model.Moment
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.toJavaInstant

@Composable
actual fun Moment.formatDate(): String {
  return SimpleDateFormat
    .getDateInstance(DateFormat.LONG, Locale.getDefault())
    .format(Date.from(instant.toJavaInstant()))
}

@Composable
actual fun Moment.formatTime(): String {
  return SimpleDateFormat
    .getTimeInstance(DateFormat.SHORT, Locale.getDefault())
    .format(Date.from(instant.toJavaInstant()))
}
