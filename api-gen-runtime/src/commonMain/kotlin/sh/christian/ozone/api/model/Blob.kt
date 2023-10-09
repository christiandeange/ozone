package sh.christian.ozone.api.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString

@OptIn(ExperimentalSerializationApi::class)
@SerialName("blob")
@Serializable
data class Blob(
  @ByteString val ref: BlobRef,
  val mimeType: String,
  val size: Long,
)

@Serializable
data class BlobRef(
  @SerialName("\$link")
  val link: String,
)
