package sh.christian.ozone.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

fun AppTypography(fontFamily: FontFamily): Typography {
  val base = Typography()

  return base.copy(
    displayLarge = base.displayLarge.copy(fontFamily = fontFamily),
    displayMedium = base.displayMedium.copy(fontFamily = fontFamily),
    displaySmall = base.displaySmall.copy(fontFamily = fontFamily),
    headlineLarge = base.headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = base.headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = base.headlineSmall.copy(fontFamily = fontFamily),
    titleLarge = base.titleLarge.copy(fontFamily = fontFamily),
    titleMedium = base.titleMedium.copy(fontFamily = fontFamily),
    titleSmall = base.titleSmall.copy(fontFamily = fontFamily),
    bodyLarge = base.bodyLarge.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
    bodyMedium = base.bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = base.bodySmall.copy(fontFamily = fontFamily),
    labelLarge = base.labelLarge.copy(fontFamily = fontFamily),
    labelMedium = base.labelMedium.copy(fontFamily = fontFamily),
    labelSmall = base.labelSmall.copy(fontFamily = fontFamily),
  )
}
