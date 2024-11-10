package sh.christian.ozone.api.xrpc

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.serializer
import sh.christian.ozone.api.response.AtpErrorDescription
import kotlin.reflect.KClass

data class XrpcSubscriptionResponse(
  val bytes: ByteArray,
) {
  @ExperimentalSerializationApi
  inline fun <reified T : Any> body(
    noinline subscriptionSerializerProvider: SubscriptionSerializerProvider<T>,
  ): T = body(T::class, subscriptionSerializerProvider)

  @OptIn(InternalSerializationApi::class)
  @ExperimentalSerializationApi
  fun <T : Any> body(
    kClass: KClass<T>,
    subscriptionSerializerProvider: SubscriptionSerializerProvider<T>,
  ): T {
    val frame = cbor.decodeFromByteArray(XrpcSubscriptionFrame.serializer(), bytes)

    val payloadPosition = bytes.drop(1).indexOfFirst { it.toInt().isCborMapStart() } + 1
    val payloadBytes = bytes.drop(payloadPosition).toByteArray()

    if (frame.op == 1 && frame.t != null) {
      val serializer = subscriptionSerializerProvider(kClass, frame.t) ?: kClass.serializer()
      return cbor.decodeFromByteArray(serializer, payloadBytes)
    } else {
      val maybeErrorDescription: AtpErrorDescription? =
        runCatching { cbor.decodeFromByteArray(AtpErrorDescription.serializer(), payloadBytes) }
          .getOrNull()

      throw XrpcSubscriptionParseException(maybeErrorDescription)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is XrpcSubscriptionResponse) return false

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
    val cbor = Cbor {
      ignoreUnknownKeys = true
      alwaysUseByteString = true
    }
  }
}

internal fun Int.isCborMapStart(): Boolean = (this and 0b11100000) == 0b10100000
