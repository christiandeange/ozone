package sh.christian.ozone.ui.compose

import android.content.res.AssetManager
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

lateinit var fontsAssetManager: AssetManager

@OptIn(ExperimentalTextApi::class)
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
