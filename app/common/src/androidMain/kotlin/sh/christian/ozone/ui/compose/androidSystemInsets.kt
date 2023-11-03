package sh.christian.ozone.ui.compose

import android.app.Activity
import android.view.WindowInsets.Type
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat.OnControllableInsetsChangedListener

@Composable
actual fun rememberSystemInsets(): PaddingValues {
  val density = LocalDensity.current
  val window = (LocalContext.current as Activity).window
  val view = window.decorView

  fun getSystemInsets(): PaddingValues {
    return with(view.rootWindowInsets.getInsets(Type.systemBars() or Type.ime())) {
      with(density) {
        PaddingValues(
          top = top.toDp(),
          bottom = bottom.toDp(),
        )
      }
    }
  }

  var insets by remember { mutableStateOf(getSystemInsets()) }

  DisposableEffect(view, density) {
    val insetsController = WindowCompat.getInsetsController(window, view)
    val listener = OnControllableInsetsChangedListener { _, _ ->
      insets = getSystemInsets()
    }

    insetsController.addOnControllableInsetsChangedListener(listener)
    onDispose {
      insetsController.removeOnControllableInsetsChangedListener(listener)
    }
  }

  return insets
}
