package sh.christian.ozone.util

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ImmutableListSerializer<T>(
  dataSerializer: KSerializer<T>,
) : KSerializer<ImmutableList<T>> {
  private val listSerializer = ListSerializer(dataSerializer)

  override val descriptor: SerialDescriptor = ImmutableListDescriptor(dataSerializer.descriptor)

  override fun serialize(
    encoder: Encoder,
    value: ImmutableList<T>,
  ) {
    return listSerializer.serialize(encoder, value.toList())
  }

  override fun deserialize(decoder: Decoder): ImmutableList<T> {
    return listSerializer.deserialize(decoder).toImmutableList()
  }

  @OptIn(ExperimentalSerializationApi::class, SealedSerializationApi::class)
  private class ImmutableListDescriptor(
    private val elementDescriptor: SerialDescriptor,
  ) : SerialDescriptor by listSerialDescriptor(elementDescriptor) {
    override val serialName: String = "kotlinx.collections.immutable.ImmutableList"
  }
}
