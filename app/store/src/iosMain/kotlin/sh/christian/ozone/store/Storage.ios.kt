package sh.christian.ozone.store

import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults
import sh.christian.ozone.store.settings.SettingsStorage

fun storage(): PersistentStorage {
  val delegate: NSUserDefaults = NSUserDefaults.standardUserDefaults()
  return SettingsStorage(NSUserDefaultsSettings(delegate))
}
