package sh.christian.ozone.store.settings

import com.russhwolf.settings.Settings
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.Preference
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
internal class SettingsStorage(
  private val settings: Settings,
) : PersistentStorage {
  override fun <T : Any> preference(
    key: String,
    defaultValue: T,
    clazz: KClass<T>,
  ): Preference<T> {
    return SettingsPreference(
      settings = settings,
      format = Json,
      name = key,
      serializer = clazz.serializer(),
      defaultValue = defaultValue,
    )
  }

  override fun <T : Any> nullablePreference(
    key: String,
    defaultValue: T?,
    clazz: KClass<T>,
  ): Preference<T?> {
    val serializer: KSerializer<T> = clazz.serializer()
    return NullableSettingsPreference(
      settings = settings,
      format = Json,
      name = key,
      serializer = serializer,
      defaultValue = defaultValue,
    )
  }
}
