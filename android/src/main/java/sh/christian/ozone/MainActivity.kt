package sh.christian.ozone

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.login.LoginWorkflow
import sh.christian.ozone.store.storage

class MainActivity : AppCompatActivity() {

  private val workflow by lazy {
    AppWorkflow(
      loginWorkflow = LoginWorkflow(
        loginRepository = LoginRepository(storage),
      )
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      App(workflow, onExit = { onBackPressedDispatcher.onBackPressed() })
    }
  }
}
