package sh.christian.ozone.app

sealed interface LoggedInOutput {
  object SignOut : LoggedInOutput
  object CloseApp : LoggedInOutput
}
