package sh.christian.ozone.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import sh.christian.ozone.model.Moment
import kotlin.time.toJSDate

@Composable
actual fun Moment.formatDate(): String {
  return instant.toJSDate().toLocaleDateString(
    locales = Locale.current.toLanguageTag(),
    options = dateLocaleOptions {
      year = "numeric"
      month = "long"
      day = "numeric"
    },
  )
}

@Composable
actual fun Moment.formatTime(): String {
  return instant.toJSDate().toLocaleTimeString(
    locales = Locale.current.toLanguageTag(),
    options = dateLocaleOptions {
      hour = "numeric"
      minute = "numeric"
    },
  )
}
