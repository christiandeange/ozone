package sh.christian.ozone.util

import androidx.compose.runtime.Composable
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterShortStyle
import sh.christian.ozone.model.Moment

@Composable
actual fun Moment.formatDate(): String {
  return NSDateFormatter()
    .apply { dateStyle = NSDateFormatterShortStyle }
    .stringFromDate(instant.toNSDate())
}

@Composable
actual fun Moment.formatTime(): String {
  return NSDateFormatter()
    .apply { timeStyle = NSDateFormatterShortStyle }
    .stringFromDate(instant.toNSDate())
}
