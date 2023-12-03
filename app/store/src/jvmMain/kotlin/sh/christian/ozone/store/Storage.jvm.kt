package sh.christian.ozone.store

import com.russhwolf.settings.PreferencesSettings
import sh.christian.ozone.store.settings.SettingsStorage
import java.util.prefs.Preferences

fun storage(): PersistentStorage {
  val preferences = Preferences.userRoot().node("sh.christian.ozone").node("1").apply { sync() }
  val settings = PreferencesSettings(preferences)
  return SettingsStorage(settings)
}
