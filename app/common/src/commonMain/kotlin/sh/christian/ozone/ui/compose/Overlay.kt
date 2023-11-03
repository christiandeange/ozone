package sh.christian.ozone.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Overlay(
  visibleState: MutableTransitionState<Boolean>,
  enter: EnterTransition,
  exit: ExitTransition,
  onClickOutside: () -> Unit,
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
          onClick = onClickOutside,
        ),
      visibleState = visibleState,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Box(Modifier.background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.38f)))
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
      visibleState = visibleState,
      enter = enter,
      exit = exit,
    ) {
      content()
    }
  }
}
