package sh.christian.ozone.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme
import kotlin.time.Duration.Companion.seconds

@Composable
actual fun DynamicDarkMode(content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalColorTheme provides ColorTheme(isSystemInDarkTheme())) {
    content()
  }
}
