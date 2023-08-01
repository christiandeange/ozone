package sh.christian.ozone.api.xrpc

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.serializer
import sh.christian.ozone.api.cbor.ByteArrayInput
import sh.christian.ozone.api.cbor.CborDecoder
import sh.christian.ozone.api.cbor.CborReader
import sh.christian.ozone.api.response.AtpErrorDescription
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

data class XrpcSubscriptionResponse(
  val bytes: ByteArray,
) {
  @ExperimentalSerializationApi
  inline fun <reified T : Any> body(): T = body(T::class)

  @ExperimentalSerializationApi
  fun <T : Any> body(kClass: KClass<T>): T {
    val info = decodeFromByteArray(XrpcSubscriptionFrame.serializer(), bytes)

    val payloadPosition = bytes.drop(1).indexOfFirst { it.toInt().isCborMapStart() } + 1
    val payloadBytes = bytes.drop(payloadPosition).toByteArray()

    if (info.op == 1 && info.t != null) {
      val subtype = kClass.sealedSubclasses.firstOrNull { subclass ->
        subclass.findAnnotation<SerialName>()!!.value.endsWith(info.t)
      } ?: kClass

      @OptIn(InternalSerializationApi::class)
      return decodeFromByteArray(subtype.serializer(), payloadBytes)
    } else {
      val maybeError = runCatching { decodeFromByteArray(AtpErrorDescription.serializer(), payloadBytes) }.getOrNull()
      throw XrpcSubscriptionParseException(maybeError)
    }
  }

  @ExperimentalSerializationApi
  private fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
    val stream = ByteArrayInput(bytes)
    val reader = CborReader(cbor, CborDecoder(stream))
    return reader.decodeSerializableValue(deserializer)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as XrpcSubscriptionResponse

    return bytes.contentEquals(other.bytes)
  }

  override fun hashCode(): Int {
    return bytes.contentHashCode()
  }

  override fun toString(): String {
    return "XrpcSubscriptionResponse(bytes=${bytes.contentToString()})"
  }

  private companion object {
    @ExperimentalSerializationApi
    val cbor = Cbor { ignoreUnknownKeys = true }
  }
}

@PublishedApi
internal fun Int.isCborMapStart(): Boolean = (this and 0b11100000) == 0b10100000
