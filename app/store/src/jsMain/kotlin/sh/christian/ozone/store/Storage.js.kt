package sh.christian.ozone.store

import com.russhwolf.settings.StorageSettings
import kotlinx.browser.localStorage
import sh.christian.ozone.store.settings.SettingsStorage

fun storage(): PersistentStorage {
  val settings = StorageSettings(delegate = localStorage)
  return SettingsStorage(settings)
}
