package sh.christian.ozone.error

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.christian.ozone.ui.compose.Overlay
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen

class ErrorScreen(
  private val title: String?,
  private val description: String?,
  private val retryable: Boolean,
  private val onDismiss: () -> Unit,
  private val onRetry: () -> Unit,
) : ViewRendering by screen({
  Overlay(
    visible = true,
    onDismiss = onDismiss,
  ) {
    Column(
      modifier = Modifier.padding(32.dp),
      verticalArrangement = spacedBy(8.dp),
    ) {
      if (title != null) {
        Text(
          text = title,
          style = MaterialTheme.typography.headlineMedium,
        )
      }
      if (description != null) {
        Text(
          text = description,
        )
      }
      Row(horizontalArrangement = spacedBy(8.dp)) {
        if (retryable) {
          Button(onRetry) {
            Text("Retry")
          }
          Button(onDismiss) {
            Text("Dismiss")
          }
        } else {
          Button(onDismiss) {
            Text("OK")
          }
        }
      }
    }
  }
})
