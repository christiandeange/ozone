package sh.christian.ozone.store

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.cbor.Cbor
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface PersistentStorage {
  fun <T> preference(
    key: String,
    defaultValue: T,
    serializer: Serializer<T>
  ): Preference<T>

  fun clear()
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> PersistentStorage.preference(
  key: String,
  defaultValue: T,
  format: SerialFormat = Cbor.Default,
): Preference<T> {
  return preference(key, defaultValue, KotlinXSerializer(format))
}

interface Preference<T> : ReadWriteProperty<Any?, T> {
  fun get(): T
  fun set(value: T)
  fun delete()
  fun asFlow(): Flow<T>

  override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()
  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)
}

interface Serializer<T> {
  fun serialize(value: T): String
  fun deserialize(serialized: String): T
}
