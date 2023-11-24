package sh.christian.ozone.store

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

internal abstract class KStorePersistentStorage : PersistentStorage {
  private val stores: MutableMap<String, KStore<*>> = mutableMapOf()

  @Suppress("UNCHECKED_CAST")
  override fun <T : @Serializable Any> preference(
    key: String,
    defaultValue: T?,
    clazz: KClass<T>,
  ): KStore<T> {
    return stores.getOrPut(key) {
      KStore(
        default = defaultValue,
        enableCache = true,
        codec = codec(key, clazz),
      )
    } as KStore<T>
  }

  internal abstract fun <T : @Serializable Any> codec(
    key: String,
    clazz: KClass<T>,
  ): Codec<T>
}
