package sh.christian.ozone.api.runtime

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import sh.christian.ozone.api.model.AtpEnum

inline fun <reified E> defaultEnumSerializer(defaultValue: E): KSerializer<E> where E : Enum<E>, E : AtpEnum {
  return DefaultEnumKSerializer(E::class.simpleName!!, enumValues<E>(), defaultValue)
}

class DefaultEnumKSerializer<E>(
  serialName: String,
  values: Array<E>,
  private val defaultValue: E,
) : KSerializer<E> where E : Enum<E>, E : AtpEnum {
  private val valuesByOrdinal: Map<Int, E> = values.associateBy { it.ordinal }

  override val descriptor: SerialDescriptor = createDescriptor(serialName, values)

  override fun deserialize(decoder: Decoder): E {
    return runCatching {
      val index = decoder.decodeEnum(descriptor)
      valuesByOrdinal[index] ?: defaultValue
    }.recover { e ->
      if (e is SerializationException) {
        defaultValue
      } else {
        throw e
      }
    }.getOrThrow()
  }

  override fun serialize(encoder: Encoder, value: E) {
    encoder.encodeEnum(descriptor, value.ordinal)
  }

  private companion object {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    fun createDescriptor(serialName: String, values: Array<out AtpEnum>): SerialDescriptor {
      val descriptor = kotlinx.serialization.internal.EnumDescriptor(serialName, values.size)
      values.forEach { v ->
        val elementName = v.value
        descriptor.addElement(elementName)
      }
      return descriptor
    }
  }
}
