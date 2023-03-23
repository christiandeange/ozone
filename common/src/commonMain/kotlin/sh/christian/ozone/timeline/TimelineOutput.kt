package sh.christian.ozone.timeline

sealed interface TimelineOutput {
  object SignOut : TimelineOutput
  object CloseApp : TimelineOutput
}
