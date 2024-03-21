package sh.christian.ozone

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sh.christian.ozone.api.OzoneDispatchers.IO
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.di.AppComponent
import sh.christian.ozone.di.create
import sh.christian.ozone.store.storage
import sh.christian.ozone.ui.AppTheme
import sh.christian.ozone.ui.compose.initTypography
import sh.christian.ozone.ui.workflow.WorkflowRendering

lateinit var workflow: AppWorkflow

fun initialize() {
  val storage = storage()
  val component = AppComponent::class.create(storage)
  workflow = component.appWorkflow

  val scope = CoroutineScope(IO)
  component.supervisors.forEach {
    with(it) {
      scope.launch(SupervisorJob()) { start() }
    }
  }

  runBlocking {
    // Ensure that this is set up before we actually use it in the theme.
    initTypography()
  }
}

fun MainViewController() = ComposeUIViewController {
  Box {
    AppTheme {
      WorkflowRendering(
        workflow = workflow,
        onOutput = { },
        content = { it.Content() },
      )
    }
  }
}
