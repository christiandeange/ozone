package sh.christian.ozone.login

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import sh.christian.ozone.ui.LocalColorTheme
import kotlin.time.Duration.Companion.seconds

@Composable
fun rememberLoginGradientBrush(): Brush {
  val sizePx = with(LocalDensity.current) { 64.dp.toPx() }

  val infiniteTransition = rememberInfiniteTransition()
  val offset by infiniteTransition.animateFloat(
    initialValue = -sizePx,
    targetValue = sizePx,
    animationSpec = infiniteRepeatable(
      tween(
        durationMillis = 5.seconds.inWholeMilliseconds.toInt(),
        easing = FastOutSlowInEasing,
      ),
      RepeatMode.Reverse
    )
  )

  val isDark = LocalColorTheme.current.isDark()
  val colors = remember(isDark) {
    if (isDark) {
      listOf(
        Color(0xFF2A5298),
        Color(0xFF1E3C72),
      )
    } else {
      listOf(
        Color(0xEBEBFF),
        Color(0xFF6BB3F0),
      )
    }
  }

  return remember(offset) {
    object : ShaderBrush() {
      override fun createShader(size: Size): Shader {
        return LinearGradientShader(
          from = Offset(offset, offset),
          to = Offset(size.width, size.height),
          colors = colors,
          tileMode = TileMode.Clamp,
        )
      }
    }
  }
}