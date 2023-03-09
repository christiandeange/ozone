package sh.christian.ozone.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// Duplicated in desktopMain
internal class DataStoreStorage(
  private val dataStore: DataStore<Preferences>,
) : PersistentStorage {
  override fun <T> preference(
    key: String,
    defaultValue: T,
    serializer: Serializer<T>,
  ): Preference<T> = DatastorePreference(
    keyName = key,
    dataStore = dataStore,
    serializer = serializer,
    defaultValue = defaultValue,
  )

  override fun clear() {
    runBlocking {
      dataStore.edit { settings -> settings.clear() }
    }
  }
}

private class DatastorePreference<T>(
  keyName: String,
  private val dataStore: DataStore<Preferences>,
  private val serializer: Serializer<T>,
  private val defaultValue: T,
) : Preference<T> {
  private val key: Key<String> = stringPreferencesKey(keyName)

  override fun get(): T {
    val value = runBlocking { dataStore.data.first()[key] } ?: return defaultValue
    return serializer.deserialize(value)
  }

  override fun set(value: T) {
    runBlocking {
      dataStore.edit { settings -> settings[key] = serializer.serialize(value) }
    }
  }

  override fun delete() {
    runBlocking {
      dataStore.edit { settings -> settings.remove(key) }
    }
  }

  override fun asFlow(): Flow<T> {
    return dataStore.data
      .map { settings ->
        settings[key]?.let { serializer.deserialize(it) } ?: defaultValue
      }
      .distinctUntilChanged()
  }
}
