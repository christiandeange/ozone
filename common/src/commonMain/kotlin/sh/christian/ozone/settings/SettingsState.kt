package sh.christian.ozone.settings

sealed interface SettingsState {
  object ShowingSettings : SettingsState

  object ConfirmSignOut : SettingsState
}
