package sh.christian.ozone.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
actual fun DynamicDarkMode(content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalColorTheme provides ColorTheme(isSystemInDarkTheme())) {
    content()
  }
}
