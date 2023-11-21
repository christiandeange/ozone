package sh.christian.ozone.api.xrpc

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@OptIn(InternalSerializationApi::class)
internal actual fun <T : Any> getSerializer(
  kClass: KClass<T>,
  frame: XrpcSubscriptionFrame,
): KSerializer<out T> {
  val subtype = kClass.sealedSubclasses.firstOrNull { subclass ->
    subclass.findAnnotation<SerialName>()!!.value.endsWith(frame.t!!)
  } ?: kClass

  return subtype.serializer()
}
