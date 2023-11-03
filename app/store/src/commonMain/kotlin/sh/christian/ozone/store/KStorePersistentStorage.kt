package sh.christian.ozone.store

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

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
        codec = FileCodec(
          filePath = pathTo(key),
          json = Json,
          serializer = Json.serializersModule.serializer(clazz.createType()) as KSerializer<T>,
        )
      )
    } as KStore<T>
  }

  override fun clear() = runBlocking {
    stores.values.forEach { it.delete() }
  }

  internal abstract fun pathTo(key: String): String
}
