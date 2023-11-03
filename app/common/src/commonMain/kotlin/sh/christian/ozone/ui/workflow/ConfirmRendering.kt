package sh.christian.ozone.ui.workflow

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.christian.ozone.ui.compose.SystemInsets
import sh.christian.ozone.ui.workflow.Dismissable.DismissHandler

class ConfirmRendering(
  private val title: String,
  private val description: String?,
  private val dismissText: String = "Cancel",
  onDismiss: () -> Unit,
  private val confirmText: String = "OK",
  private val onConfirm: () -> Unit,
) : OverlayRendering by overlay(DismissHandler(onDismiss), { onRequestDismiss ->
  SystemInsets {
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shadowElevation = 16.dp,
    ) {
      Column(modifier = Modifier.padding(32.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.headlineMedium,
        )

        if (description != null) {
          Text(
            modifier = Modifier.padding(top = 8.dp),
            text = description,
          )
        }

        Row(
          modifier = Modifier.padding(top = 32.dp),
          horizontalArrangement = spacedBy(8.dp),
        ) {
          Button(onRequestDismiss) {
            Text(dismissText)
          }
          Button(onConfirm) {
            Text(confirmText)
          }
        }
      }
    }
  }
})
