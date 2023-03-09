package sh.christian.ozone.store

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.serializer

class KotlinXSerializer<T>(
  private val format: SerialFormat,
  private val serializer: KSerializer<T>,
) : Serializer<T> {
  override fun deserialize(serialized: String): T {
    return when (format) {
      is StringFormat -> format.decodeFromString(serializer, serialized)
      is BinaryFormat -> format.decodeFromHexString(serializer, serialized)
      else -> error("Unknown serial format: $format")
    }
  }

  override fun serialize(value: T): String {
    return when (format) {
      is StringFormat -> format.encodeToString(serializer, value)
      is BinaryFormat -> format.encodeToHexString(serializer, value)
      else -> error("Unknown serial format: $format")
    }
  }

  companion object {
    inline operator fun <reified T> invoke(format: SerialFormat): KotlinXSerializer<T> {
      return KotlinXSerializer(format, format.serializersModule.serializer())
    }
  }
}
