package sh.christian.ozone.ui.renderer

import androidx.compose.runtime.Composable
import software.ralf.app.platform.presenter.BaseModel

/**
 * A screen model rendered by its matching [software.ralf.app.platform.renderer.ComposeRenderer].
 *
 * The temporary [Content] hook keeps the established screen layouts intact while their layout
 * code is moved out of models one feature at a time.
 */
interface ScreenModel : BaseModel {
  @Composable
  fun Content()
}

fun screen(content: @Composable () -> Unit): ScreenModel = object : ScreenModel {
  @Composable
  override fun Content() = content()
}
