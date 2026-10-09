package sh.christian.ozone

import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import dev.marcellogalhardo.retained.activity.retain
import sh.christian.ozone.app.OzoneTemplateRenderer
import sh.christian.ozone.app.initOzone
import sh.christian.ozone.store.storage
import sh.christian.ozone.ui.AppTheme

class MainActivity : AppCompatActivity() {
  private val ozone by retain { initOzone(lifecycleScope, storage) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window?.statusBarColor = Color.TRANSPARENT
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      AppTheme {
        StatusBarTheme()
        val template by ozone.templates.collectAsState()
        OzoneTemplateRenderer().renderCompose(template)
      }
    }
  }

  override fun onDestroy() {
    // retain survives configuration changes, so only the final Activity closes the root scope.
    if (isFinishing && !isChangingConfigurations) ozone.close()
    super.onDestroy()
  }
}
