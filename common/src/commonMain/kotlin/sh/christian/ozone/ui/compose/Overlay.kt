package sh.christian.ozone.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Overlay(
  visible: Boolean,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit = {},
) {
  Box(modifier = modifier) {
    AnimatedVisibility(
      modifier = Modifier
        .fillMaxSize()
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss,
        ),
      visible = visible,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Surface(
        color = Color.Black.copy(alpha = 0.38f),
        content = {},
      )
    }

    AnimatedVisibility(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = {},
        ),
      visible = visible,
      enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
      exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
    ) {
      Surface(shadowElevation = 16.dp) {
        content()
      }
    }
  }
}
