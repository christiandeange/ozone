package sh.christian.ozone.store

import ca.gosyer.appdirs.AppDirs
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
fun storage(): PersistentStorage {
  val filesDir: Path =
    Paths.get(AppDirs("sh.christian.ozone", "1").getUserDataDir()).resolve("storage")

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
