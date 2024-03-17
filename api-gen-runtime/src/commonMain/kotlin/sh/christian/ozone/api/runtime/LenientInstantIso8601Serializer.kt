package sh.christian.ozone.api.runtime

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LenientInstantIso8601Serializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): Instant {
    val raw = decoder.decodeString()
    val firstParseAttempt = runCatching { Instant.parse(raw) }

    return firstParseAttempt
      .recoverCatching { Instant.parse(raw + "Z") }
      .fold(
        onSuccess = { it },
        onFailure = { throw firstParseAttempt.exceptionOrNull()!! }
      )
  }

  override fun serialize(encoder: Encoder, value: Instant) {
    encoder.encodeString(value.toString())
  }
}