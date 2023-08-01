package sh.christian.ozone.api.xrpc

import kotlinx.serialization.Serializable

@Serializable
internal data class XrpcSubscriptionFrame(
  val op: Int,
  val t: String?,
)
