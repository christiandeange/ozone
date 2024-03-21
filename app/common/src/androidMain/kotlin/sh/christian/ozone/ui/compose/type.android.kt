package sh.christian.ozone.ui.compose

import android.content.res.AssetManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

lateinit var fontsAssetManager: AssetManager

actual suspend fun heroFont(): FontFamily {
  return FontFamily(
    Font(
      "Syne.ttf",
      fontsAssetManager,
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
          "Rubik-$weightName$styleName.ttf",
          fontsAssetManager,
          weight = weight,
          style = style,
        )
      }
    }
  )
}
