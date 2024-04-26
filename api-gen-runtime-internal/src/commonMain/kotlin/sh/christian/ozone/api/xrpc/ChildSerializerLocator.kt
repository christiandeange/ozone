package sh.christian.ozone.api.xrpc

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

typealias SubscriptionSerializerProvider<T> = (KClass<T>, String) -> KSerializer<T>?
