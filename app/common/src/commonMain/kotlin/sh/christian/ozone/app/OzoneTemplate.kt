package sh.christian.ozone.app

import software.ralf.app.platform.presenter.BaseModel
import software.ralf.app.platform.presenter.template.Template
import sh.christian.ozone.ui.compose.OpenImageAction

sealed interface OzoneTemplate : Template {
  data class FullScreen(val content: BaseModel) : OzoneTemplate

  data class Modal(
    val content: BaseModel,
    val modal: OzoneModalModel,
  ) : OzoneTemplate
}

interface OzoneModalModel : BaseModel {
  val onRequestDismiss: (() -> Unit)?
}

/** Full-screen image gallery presented above the current screen. */
data class ImageModalModel(
  val action: OpenImageAction,
  override val onRequestDismiss: (() -> Unit)?,
) : OzoneModalModel
