package sh.christian.ozone.ui.compose

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import org.jetbrains.skiko.loadBytesFromPath

actual suspend fun heroFont(): FontFamily {
  return FontFamily(
    Font(
      identity = "Syne-Bold",
      data = loadBytesFromPath("Syne.ttf"),
      weight = FontWeight.Bold,
      style = FontStyle.Normal,
    )
  )
}

actual suspend fun appFont(): FontFamily {
  return FontFamily(
    weights.flatMap { (weight, weightName) ->
      styles.map { (style, styleName) ->
        Font(
          identity = "Rubik-$weightName$styleName",
          data = loadBytesFromPath("Rubik-$weightName$styleName.ttf"),
          weight = weight,
          style = style,
        )
      }
    }
  )
}
