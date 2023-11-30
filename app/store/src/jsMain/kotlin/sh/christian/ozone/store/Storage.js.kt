package sh.christian.ozone.store

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.storage.StorageCodec
import kotlinx.browser.localStorage
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
fun storage(): PersistentStorage {
  return object : KStorePersistentStorage() {
    override fun <T : Any> codec(key: String, clazz: KClass<T>): Codec<T> {
      return StorageCodec(
        key = key,
        json = Json,
        serializer = clazz.serializer(),
        storage = localStorage,
      )
    }
  }
}
