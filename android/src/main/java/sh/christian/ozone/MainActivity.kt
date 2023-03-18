package sh.christian.ozone

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dev.marcellogalhardo.retained.activity.retain
import sh.christian.ozone.store.storage

class MainActivity : AppCompatActivity() {

  private val appComponent by retain { AppComponent(storage) }
  private val workflow by lazy { appComponent.appWorkflow }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    appComponent.supervisors.forEach { supervisor ->
      with(supervisor) {
        lifecycleScope.onStart()
      }
    }

    setContent {
      App(workflow, onExit = { onBackPressedDispatcher.onBackPressed() })
    }
  }
}
