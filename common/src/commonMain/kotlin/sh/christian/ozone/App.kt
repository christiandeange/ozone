package sh.christian.ozone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.ui.AppTheme
import sh.christian.ozone.ui.workflow.WorkflowRendering

@Composable
fun App(
  workflow: AppWorkflow,
  onExit: () -> Unit,
) {
  AppTheme {
    WorkflowRendering(
      workflow = workflow,
      props = Unit,
      onOutput = { onExit() },
    ) { screen ->
      Surface(Modifier.fillMaxSize()) {
        Box {
          screen.Content()
        }
      }
    }
  }
}
