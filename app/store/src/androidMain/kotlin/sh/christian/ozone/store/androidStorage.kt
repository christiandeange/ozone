package sh.christian.ozone.store

import android.content.Context
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
val Context.storage: PersistentStorage
  get() {
    val filesDir = filesDir.resolve("storage")

    return object : KStorePersistentStorage() {
      override fun <T : @Serializable Any> codec(
        key: String,
        clazz: KClass<T>,
      ): Codec<T> {
        return FileCodec(
          filePath = filesDir.resolve("$key.json").toString(),
          json = Json,
          serializer = clazz.serializer(),
        )
      }
    }
  }
