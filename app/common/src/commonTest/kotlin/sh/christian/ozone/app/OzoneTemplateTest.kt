package sh.christian.ozone.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.collections.immutable.persistentListOf
import sh.christian.ozone.ui.compose.BasicImage
import sh.christian.ozone.ui.compose.OpenImageAction
import software.ralf.app.platform.presenter.BaseModel

class OzoneTemplateTest {
  @Test
  fun fullScreenPreservesItsScreenModel() {
    val screen = TestModel

    val template = OzoneTemplate.FullScreen(screen)

    assertSame(screen, template.content)
  }

  @Test
  fun modalPreservesItsScreenAndDismissContract() {
    val screen = TestModel
    var dismisses = 0
    val modal = object : OzoneModalModel {
      override val onRequestDismiss: () -> Unit = { dismisses++ }
    }

    val template = OzoneTemplate.Modal(screen, modal)
    template.modal.onRequestDismiss?.invoke()

    assertSame(screen, template.content)
    assertSame(modal, template.modal)
    assertEquals(1, dismisses)
  }

  @Test
  fun imageModalPreservesTheSelectedImageAndDismissContract() {
    var dismisses = 0
    val action = OpenImageAction(
      images = persistentListOf(BasicImage("https://example.com/image.jpg", "An image")),
      selectedIndex = 0,
    )

    val modal = ImageModalModel(action) { dismisses++ }
    modal.onRequestDismiss?.invoke()

    assertSame(action, modal.action)
    assertEquals(1, dismisses)
  }

  private data object TestModel : BaseModel
}
