package sh.christian.ozone.error

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.christian.ozone.ui.compose.SystemInsets
import sh.christian.ozone.ui.workflow.Dismissable.DismissHandler
import sh.christian.ozone.ui.workflow.OverlayRendering
import sh.christian.ozone.ui.workflow.overlay

class ErrorScreen(
  private val title: String?,
  private val description: String?,
  private val retryable: Boolean,
  onDismiss: () -> Unit,
  private val onRetry: () -> Unit,
) : OverlayRendering by overlay(DismissHandler(onDismiss), { onRequestDismiss ->
  SystemInsets {
    Surface(shadowElevation = 16.dp) {
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
          Text(text = description)
        }

        Row(
          modifier = Modifier.padding(32.dp),
          horizontalArrangement = spacedBy(8.dp),
        ) {
          if (retryable) {
            Button(onRetry) {
              Text("Retry")
            }
            Button(onRequestDismiss) {
              Text("Dismiss")
            }
          } else {
            Button(onRequestDismiss) {
              Text("OK")
            }
          }
        }
      }
    }
  }
})
