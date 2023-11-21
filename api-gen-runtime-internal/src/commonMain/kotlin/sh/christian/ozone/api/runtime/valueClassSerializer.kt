package sh.christian.ozone.api.runtime

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Workaround for https://github.com/Kotlin/kotlinx.serialization/issues/2288
 *
 * When serializing a sealed value class, the serial name of the inner class is embedded into the
 * resulting JSON string. However, the produced string cannot be deserialized, as it is the outer
 * class's serial name (ie: the sealed value class itself) that should be used here.
 */
fun <T : Any, U : Any> valueClassSerializer(
  serialName: String,
  constructor: (U) -> T,
  valueProvider: (T) -> U,
  valueSerializerProvider: () -> KSerializer<U>,
): KSerializer<T> {
  return SealedValueClassSerialNameKSerializer(serialName, constructor, valueProvider, valueSerializerProvider)
}

@OptIn(ExperimentalSerializationApi::class)
private class SealedValueClassSerialNameKSerializer<T : Any, U : Any>(
  private val serialName: String,
  private val constructor: (U) -> T,
  private val valueProvider: (T) -> U,
  valueSerializerProvider: () -> KSerializer<U>,
) : KSerializer<T> {
  private val valueSerializer: KSerializer<U> by lazy(valueSerializerProvider)

  private val descriptorMapping: Map<SerialDescriptor, SerialDescriptor> by lazy {
    mapOf(valueSerializer.descriptor to SerialDescriptor(serialName, valueSerializer.descriptor))
  }

  override val descriptor: SerialDescriptor
    get() = buildClassSerialDescriptor(serialName)

  override fun serialize(
    encoder: Encoder,
    value: T,
  ) {
    val overrideEncoder = OverrideDescriptorEncoder(descriptorMapping, encoder)
    valueSerializer.serialize(overrideEncoder, valueProvider(value))
  }

  override fun deserialize(decoder: Decoder): T {
    val overrideDecoder = OverrideDescriptorDecoder(descriptorMapping, decoder)
    return constructor(valueSerializer.deserialize(overrideDecoder.decodeInline(descriptor)))
  }
}

private class OverrideDescriptorEncoder(
  private val descriptorMapping: Map<SerialDescriptor, SerialDescriptor>,
  private val base: Encoder,
) : Encoder by base {
  override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
    return base.beginStructure(descriptorMapping[descriptor] ?: descriptor)
  }
}

private class OverrideDescriptorDecoder(
  private val descriptorMapping: Map<SerialDescriptor, SerialDescriptor>,
  private val base: Decoder,
) : Decoder by base {
  override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
    return base.beginStructure(descriptorMapping[descriptor] ?: descriptor)
  }
}
