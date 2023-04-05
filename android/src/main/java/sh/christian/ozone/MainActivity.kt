package sh.christian.ozone

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dev.marcellogalhardo.retained.activity.retain
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sh.christian.ozone.store.storage
import sh.christian.ozone.ui.AppTheme
import sh.christian.ozone.ui.compose.fontsAssetManager
import sh.christian.ozone.ui.compose.initTypography

class MainActivity : AppCompatActivity() {

  private val appComponent by retain { AppComponent(storage) }
  private val workflow by lazy { appComponent.appWorkflow }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    appComponent.supervisors.forEach { supervisor ->
      with(supervisor) {
        lifecycleScope.launch { onStart() }
      }
    }

    runBlocking {
      // Ensure that this is set up before we actually use it in the theme.
      fontsAssetManager = assets
      initTypography()
    }

    setContent {
      AppTheme {
        StatusBarTheme()
        App(workflow, onExit = { onBackPressedDispatcher.onBackPressed() })
      }
    }
  }
}
