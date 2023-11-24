package sh.christian.ozone.ui.compose

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.painter.Painter
import kotlin.jvm.JvmInline

@Stable
@JvmInline
value class StablePainter(
  val painter: Painter,
)

val Painter.stable: StablePainter
  get() = StablePainter(this)
