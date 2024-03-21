package sh.christian.ozone.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberSystemInsets(): PaddingValues {
  val window = UIApplication.sharedApplication.keyWindow!!

  return window.safeAreaInsets.useContents {
    PaddingValues(
      start = left.dp,
      top = top.dp,
      end = right.dp,
      bottom = bottom.dp,
    )
  }
}
