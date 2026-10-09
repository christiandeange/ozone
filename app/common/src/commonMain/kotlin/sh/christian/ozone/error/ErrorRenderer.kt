package sh.christian.ozone.error

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.christian.ozone.ui.compose.SystemInsets
import software.ralf.app.platform.inject.ContributesRenderer
import software.ralf.app.platform.renderer.ComposeRenderer

@ContributesRenderer
class ErrorRenderer : ComposeRenderer<ErrorPresenter.Model>() {
  @Composable
  override fun Compose(model: ErrorPresenter.Model, modifier: Modifier) {
    SystemInsets {
      Surface(modifier = modifier, shadowElevation = 16.dp) {
        Column(
          modifier = Modifier.padding(32.dp),
          verticalArrangement = spacedBy(8.dp),
        ) {
          model.title?.let { Text(it, style = MaterialTheme.typography.headlineMedium) }
          model.description?.let { description -> Text(description) }
          Row(
            modifier = Modifier.padding(32.dp),
            horizontalArrangement = spacedBy(8.dp),
          ) {
            if (model.retryable) Button(model.onRetry) { Text("Retry") }
            Button(model.onRequestDismiss ?: {}) {
              Text(if (model.retryable) "Dismiss" else "OK")
            }
          }
        }
      }
    }
  }
}
