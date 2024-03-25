package sh.christian.ozone.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import sh.christian.ozone.common.generated.resources.Res
import sh.christian.ozone.common.generated.resources.Rubik_Black
import sh.christian.ozone.common.generated.resources.Rubik_BlackItalic
import sh.christian.ozone.common.generated.resources.Rubik_Bold
import sh.christian.ozone.common.generated.resources.Rubik_BoldItalic
import sh.christian.ozone.common.generated.resources.Rubik_ExtraBold
import sh.christian.ozone.common.generated.resources.Rubik_ExtraBoldItalic
import sh.christian.ozone.common.generated.resources.Rubik_Light
import sh.christian.ozone.common.generated.resources.Rubik_LightItalic
import sh.christian.ozone.common.generated.resources.Rubik_Medium
import sh.christian.ozone.common.generated.resources.Rubik_MediumItalic
import sh.christian.ozone.common.generated.resources.Rubik_Normal
import sh.christian.ozone.common.generated.resources.Rubik_NormalItalic
import sh.christian.ozone.common.generated.resources.Rubik_SemiBold
import sh.christian.ozone.common.generated.resources.Rubik_SemiBoldItalic
import sh.christian.ozone.common.generated.resources.Syne

@OptIn(ExperimentalResourceApi::class)
@Composable
fun heroFont(): FontFamily {
  return FontFamily(
    Font(
      resource = Res.font.Syne,
      weight = FontWeight.Bold,
      style = FontStyle.Normal,
    )
  )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun appFont(): FontFamily {
  return FontFamily(
    Font(
      resource = Res.font.Rubik_Black,
      weight = FontWeight.Black,
      style = FontStyle.Normal,
    ),
    Font(
      resource = Res.font.Rubik_BlackItalic,
      weight = FontWeight.Black,
      style = FontStyle.Italic,
    ),
    Font(
      resource = Res.font.Rubik_Bold,
      weight = FontWeight.Bold,
      style = FontStyle.Normal,
    ),
    Font(
      resource = Res.font.Rubik_BoldItalic,
      weight = FontWeight.Bold,
      style = FontStyle.Italic,
    ),
    Font(
      resource = Res.font.Rubik_ExtraBold,
      weight = FontWeight.ExtraBold,
      style = FontStyle.Normal,
    ),
    Font(
      resource = Res.font.Rubik_ExtraBoldItalic,
      weight = FontWeight.ExtraBold,
      style = FontStyle.Italic,
    ),
    Font(
      resource = Res.font.Rubik_Light,
      weight = FontWeight.Light,
      style = FontStyle.Normal,
    ),
    Font(
      resource = Res.font.Rubik_LightItalic,
      weight = FontWeight.Light,
      style = FontStyle.Italic,
    ),
    Font(
      resource = Res.font.Rubik_Medium,
      weight = FontWeight.Medium,
      style = FontStyle.Normal,
    ),
    Font(
      resource = Res.font.Rubik_MediumItalic,
      weight = FontWeight.Medium,
      style = FontStyle.Italic,
    ),
    Font(
      resource = Res.font.Rubik_Normal,
      weight = FontWeight.Normal,
      style = FontStyle.Normal,
    ),
    Font(
      resource = Res.font.Rubik_NormalItalic,
      weight = FontWeight.Normal,
      style = FontStyle.Italic,
    ),
    Font(
      resource = Res.font.Rubik_SemiBold,
      weight = FontWeight.SemiBold,
      style = FontStyle.Normal,
    ),
    Font(
      resource = Res.font.Rubik_SemiBoldItalic,
      weight = FontWeight.SemiBold,
      style = FontStyle.Italic,
    ),
  )
}
