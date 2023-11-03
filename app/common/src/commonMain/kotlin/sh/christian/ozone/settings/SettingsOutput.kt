package sh.christian.ozone.settings

sealed interface SettingsOutput {
  object SignOut : SettingsOutput

  object CloseApp : SettingsOutput
}
