package sh.christian.ozone.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.OverlayRendering
import sh.christian.ozone.ui.workflow.overlay

class TextOverlayScreen(
  onDismiss: Dismissable,
  private val text: String,
) : OverlayRendering by overlay(onDismiss, { _ ->
  SystemInsets {
    Surface(shadowElevation = 16.dp) {
      Column(
        modifier = Modifier
          .padding(32.dp)
          .fillMaxWidth(),
      ) {
        Text(text)
      }
    }
  }
})
