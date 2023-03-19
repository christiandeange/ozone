package sh.christian.ozone.ui.compose

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

actual suspend fun appFont(): FontFamily {
  return FontFamily(
    weights.flatMap { (weight, weightName) ->
      styles.map { (style, styleName) ->
        Font(
          file = file("Rubik-$weightName$styleName.ttf",),
          weight = weight,
          style = style,
        )
      }
    }
  )
}

private fun file(fileName: String): File {
  val resourceStream: InputStream =
    Thread.currentThread().contextClassLoader.getResourceAsStream(fileName)
      ?: throw FileNotFoundException("Couldn't find font resource at: $fileName")

  return resourceStream.use { inputStream ->
    val file = File.createTempFile("font-cache", null)
    file.deleteOnExit()

    file.outputStream().use { outputStream ->
      inputStream.copyTo(outputStream)
    }

    file
  }
}
