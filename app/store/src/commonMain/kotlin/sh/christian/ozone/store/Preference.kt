package sh.christian.ozone.store

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KProperty

interface Preference<T> {
  val updates: Flow<T>

  fun get(): T
  fun set(value: T)
  fun delete()

  operator fun setValue(
    thisRef: Any?,
    property: KProperty<*>,
    value: T,
  ) {
    set(value)
  }

  operator fun getValue(
    thisRef: Any?,
    property: KProperty<*>,
  ): T {
    return get()
  }
}
