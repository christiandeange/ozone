package sh.christian.ozone.app

import androidx.compose.runtime.Composable
import software.ralf.app.platform.presenter.BaseModel
import software.ralf.app.platform.presenter.compose.ComposePresenter
import software.ralf.app.platform.presenter.template.toTemplate

/** Wraps the active root screen in Ozone's app-level template hierarchy. */
class OzoneTemplatePresenter(
  private val rootPresenter: ComposePresenter<Unit, out BaseModel>,
) : ComposePresenter<Unit, OzoneTemplate> {
  @Composable
  override fun present(input: Unit): OzoneTemplate {
    val model = rootPresenter.present(Unit)
    return model as? OzoneTemplate ?: model.toTemplate<OzoneTemplate> { OzoneTemplate.FullScreen(it) }
  }
}
