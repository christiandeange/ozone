package sh.christian.ozone.store.settings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import com.russhwolf.settings.serialization.removeValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import sh.christian.ozone.store.Preference

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
internal class NullableSettingsPreference<T : Any>(
  private val settings: Settings,
  private val format: StringFormat,
  private val name: String,
  private val serializer: KSerializer<T>,
  private val defaultValue: T?,
) : Preference<T?> {
  private val _state = MutableStateFlow(get())

  override val updates: Flow<T?> get() = _state

  override fun get(): T? {
    return settings.decodeValueOrNull(serializer, name, format.serializersModule)
      ?: defaultValue.takeUnless { settings.hasKey(name) }
  }

  override fun set(value: T?) {
    if (value == null) {
      settings.remove(name)
      _state.value = null
    } else {
      settings.encodeValue(serializer, name, value, format.serializersModule)
      _state.value = value
    }
  }

  override fun delete() {
    settings.removeValue(serializer, name, serializersModule = format.serializersModule)
    _state.value = defaultValue
  }
}
