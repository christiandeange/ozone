package sh.christian.ozone

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.CoroutineScope
import sh.christian.ozone.api.OzoneDispatchers.IO
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.app.initWorkflow
import sh.christian.ozone.store.storage
import sh.christian.ozone.ui.AppTheme
import sh.christian.ozone.ui.workflow.WorkflowRendering

lateinit var workflow: AppWorkflow

@Suppress("unused") // Called from iOS application code.
fun initialize() {
  workflow = initWorkflow(CoroutineScope(IO), storage())
}

@Suppress("unused", "FunctionName") // Called from iOS application code.
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
