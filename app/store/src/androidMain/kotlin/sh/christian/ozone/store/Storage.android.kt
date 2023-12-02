package sh.christian.ozone.store

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.russhwolf.settings.SharedPreferencesSettings
import sh.christian.ozone.store.settings.SettingsStorage

val Context.storage: PersistentStorage
  get() {
    val sharedPreferences = getSharedPreferences("prefs-storage", MODE_PRIVATE)
    val settings = SharedPreferencesSettings(sharedPreferences)
    return SettingsStorage(settings)
  }
