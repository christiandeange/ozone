package sh.christian.ozone.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.christian.ozone.ui.compose.Dismissable.DismissHandler
import sh.christian.ozone.ui.compose.Dismissable.Ignore
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen

class OverlayScreen(
  private val text: String,
  private val onDismiss: Dismissable
) : ViewRendering by screen({
  Overlay(
    visible = true,
    onDismiss = {
      when (onDismiss) {
        is DismissHandler -> onDismiss.handler()
        is Ignore -> Unit
      }
    },
  ) {
    Column(
      modifier = Modifier
        .padding(32.dp)
        .fillMaxWidth(),
    ) {
      Text(text)
    }
  }
})

sealed interface Dismissable {
  object Ignore : Dismissable
  data class DismissHandler(val handler: () -> Unit) : Dismissable
}
