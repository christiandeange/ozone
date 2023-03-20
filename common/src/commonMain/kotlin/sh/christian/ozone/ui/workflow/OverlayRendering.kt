package sh.christian.ozone.ui.workflow

interface OverlayRendering : ViewRendering {
  val onDismiss: Dismissable
}

sealed interface Dismissable {
  object Ignore : Dismissable
  data class DismissHandler(val handler: () -> Unit) : Dismissable
}
