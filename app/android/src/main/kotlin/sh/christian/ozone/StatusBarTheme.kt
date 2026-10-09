package sh.christian.ozone

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import sh.christian.ozone.ui.LocalColorTheme

@Composable
fun StatusBarTheme() {
  val window = checkNotNull(LocalActivity.current).window
  val view = window.decorView

  if (!view.isInEditMode) {
    val lightTheme = LocalColorTheme.current.isLight()

    SideEffect {
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = lightTheme
    }
  }
}
