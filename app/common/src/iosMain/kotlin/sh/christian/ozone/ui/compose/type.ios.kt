package sh.christian.ozone.ui.compose

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import platform.Foundation.NSBundle

actual suspend fun heroFont(): FontFamily {
  return FontFamily(
    Font(
      identity = "Syne-Bold",
      data = loadBytes("Syne.ttf"),
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
          data = loadBytes("Rubik-$weightName$styleName.ttf"),
          weight = weight,
          style = style,
        )
      }
    }
  )
}

private fun loadBytes(path: String): ByteArray {
  val fullPath = NSBundle.mainBundle.resourcePath + "/compose-resources/" + path
  return FileSystem.SYSTEM.source(fullPath.toPath()).buffer().readByteArray()
}
