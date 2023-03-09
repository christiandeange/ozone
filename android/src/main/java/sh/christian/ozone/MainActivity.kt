package sh.christian.ozone

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.login.LoginWorkflow

class MainActivity : AppCompatActivity() {

  private val workflow = AppWorkflow(
    loginWorkflow = LoginWorkflow()
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      App(workflow, onExit = { onBackPressedDispatcher.onBackPressed() })
    }
  }
}
