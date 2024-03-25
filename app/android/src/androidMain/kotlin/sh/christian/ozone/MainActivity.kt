package sh.christian.ozone

import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import dev.marcellogalhardo.retained.activity.retain
import sh.christian.ozone.app.initWorkflow
import sh.christian.ozone.store.storage
import sh.christian.ozone.ui.AppTheme
import sh.christian.ozone.ui.workflow.WorkflowRendering

class MainActivity : AppCompatActivity() {
  private val workflow by retain { initWorkflow(lifecycleScope, storage) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window?.statusBarColor = Color.TRANSPARENT
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      AppTheme {
        StatusBarTheme()
        WorkflowRendering(
          workflow = workflow,
          onOutput = { finish() },
          content = { it.Content() },
        )
      }
    }
  }
}
