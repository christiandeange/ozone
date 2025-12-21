package sh.christian.ozone.util

import androidx.compose.runtime.Composable
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.distantFuture
import platform.Foundation.distantPast
import platform.Foundation.timeIntervalSince1970
import sh.christian.ozone.model.Moment
import kotlin.time.Instant

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

private fun Instant.toNSDate(): NSDate {
    val secs = epochSeconds * 1.0 + nanosecondsOfSecond / 1.0e9
    if (secs < NSDate.distantPast.timeIntervalSince1970 || secs > NSDate.distantFuture.timeIntervalSince1970) {
        throw IllegalArgumentException("Boundaries of NSDate exceeded")
    }
    return NSDate.dateWithTimeIntervalSince1970(secs)
}
