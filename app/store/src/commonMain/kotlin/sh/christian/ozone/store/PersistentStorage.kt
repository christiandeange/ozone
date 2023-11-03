package sh.christian.ozone.store

import io.github.xxfast.kstore.KStore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

interface PersistentStorage {
  fun <T : @Serializable Any> preference(
    key: String,
    defaultValue: T?,
    clazz: KClass<T>,
  ): KStore<T>

  fun clear()
}

inline fun <reified T : @Serializable Any> PersistentStorage.preference(
  key: String,
  defaultValue: T?,
): KStore<T> {
  return preference(key, defaultValue, T::class)
}

operator fun <T : @Serializable Any> KStore<T>.setValue(
  thisRef: Any?,
  property: KProperty<*>,
  value: T?,
) {
  runBlocking { set(value) }
}

operator fun <T : @Serializable Any> KStore<T>.getValue(
  thisRef: Any?,
  property: KProperty<*>,
): T? {
  return runBlocking { get() }
}
