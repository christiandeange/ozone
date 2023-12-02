package sh.christian.ozone.store

import kotlin.reflect.KClass

interface PersistentStorage {
  fun <T : Any> preference(
    key: String,
    defaultValue: T,
    clazz: KClass<T>,
  ): Preference<T>

  fun <T : Any> nullablePreference(
    key: String,
    defaultValue: T?,
    clazz: KClass<T>,
  ): Preference<T?>
}

inline fun <reified T : Any> PersistentStorage.preference(
  key: String,
  defaultValue: T,
): Preference<T> {
  return preference(key, defaultValue, T::class)
}

inline fun <reified T : Any> PersistentStorage.nullablePreference(
  key: String,
  defaultValue: T?,
): Preference<T?> {
  return nullablePreference(key, defaultValue, T::class)
}
