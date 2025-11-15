package sh.christian.ozone.util

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import sh.christian.ozone.model.Moment
import java.util.Date
import kotlin.time.toJavaInstant

@Composable
actual fun Moment.formatDate(): String {
  return DateFormat
    .getDateFormat(LocalContext.current)
    .format(Date.from(instant.toJavaInstant()))
}

@Composable
actual fun Moment.formatTime(): String {
  return DateFormat
    .getTimeFormat(LocalContext.current)
    .format(Date.from(instant.toJavaInstant()))
}
