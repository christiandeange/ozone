package sh.christian.ozone.ui.compose

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

internal val weights: Map<FontWeight, String> = mapOf(
  FontWeight.Light to "Light",
  FontWeight.Normal to "Normal",
  FontWeight.Medium to "Medium",
  FontWeight.SemiBold to "SemiBold",
  FontWeight.Bold to "Bold",
  FontWeight.ExtraBold to "ExtraBold",
  FontWeight.Black to "Black",
)

internal val styles: Map<FontStyle, String> = mapOf(
  FontStyle.Normal to "",
  FontStyle.Italic to "Italic",
)

internal lateinit var appFont: FontFamily
  private set

suspend fun initTypography() {
  appFont = appFont()
}

expect suspend fun appFont(): FontFamily
