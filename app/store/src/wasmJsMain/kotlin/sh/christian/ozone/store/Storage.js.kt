package sh.christian.ozone.store

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

fun storage(): PersistentStorage {
  return WasmStorage()
}

private class WasmStorage : PersistentStorage {
  private val values = mutableMapOf<String, Any?>()

  override fun <T : Any> preference(key: String, defaultValue: T, clazz: KClass<T>): Preference<T> {
    @Suppress("UNCHECKED_CAST")
    return WasmPreference(key, defaultValue) { values[key] as T? ?: defaultValue }
  }

  override fun <T : Any> nullablePreference(key: String, defaultValue: T?, clazz: KClass<T>): Preference<T?> {
    @Suppress("UNCHECKED_CAST")
    return WasmPreference(key, defaultValue) { values[key] as T? ?: defaultValue }
  }

  private inner class WasmPreference<T>(
    private val key: String,
    private val defaultValue: T,
    initial: () -> T,
  ) : Preference<T> {
    private val state = MutableStateFlow(initial())
    override val updates: Flow<T> get() = state
    override fun get(): T = state.value
    override fun set(value: T) {
      values[key] = value
      state.value = value
    }
    override fun delete() {
      values.remove(key)
      state.value = defaultValue
    }
  }
}
