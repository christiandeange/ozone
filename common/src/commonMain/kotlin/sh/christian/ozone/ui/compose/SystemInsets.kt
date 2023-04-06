package sh.christian.ozone.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
expect fun rememberSystemInsets(): PaddingValues

@Composable
fun SystemInsets(
  modifier: Modifier = Modifier,
  contentAlignment: Alignment = Alignment.TopStart,
  propagateMinConstraints: Boolean = false,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = Modifier.padding(rememberSystemInsets()).then(modifier),
    contentAlignment = contentAlignment,
    propagateMinConstraints = propagateMinConstraints,
    content = content,
  )
}
