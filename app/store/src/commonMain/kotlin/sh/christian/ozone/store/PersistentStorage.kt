package sh.christian.ozone.store

import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

interface PersistentStorage {
  fun <T : @Serializable Any> preference(
    key: String,
    defaultValue: T?,
    clazz: KClass<T>,
  ): KStore<T>
}

inline fun <reified T : @Serializable Any> PersistentStorage.preference(
  key: String,
  defaultValue: T?,
): KStore<T> {
  return preference(key, defaultValue, T::class)
}
