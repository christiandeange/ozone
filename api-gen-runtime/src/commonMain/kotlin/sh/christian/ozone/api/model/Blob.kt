package sh.christian.ozone.api.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString
import sh.christian.ozone.api.runtime.BlobSerializer

/**
 * References to ["blobs"](https://atproto.com/specs/data-model#blob-type) (arbitrary files) have a consistent format
 * in atproto, and can be detected and processed without access to any specific Lexicon. That is, it is possible to
 * parse nodes and extract any blob references without knowing the schema.
 */
@SerialName("blob")
@Serializable(with = BlobSerializer::class)
sealed interface Blob {

  @OptIn(ExperimentalSerializationApi::class)
  @Serializable
  data class StandardBlob(
    @ByteString val ref: BlobRef,
    val mimeType: String,
    val size: Long,
  ) : Blob {
		@SerialName("\$type")
		@EncodeDefault
		val type = "blob"
  }

  @Serializable
  data class LegacyBlob(
    val cid: String,
    val mimeType: String,
  ) : Blob {
		@SerialName("\$type")
		@EncodeDefault
		val type = "blob"
  }
}

@Serializable
data class BlobRef(
  @SerialName("\$link")
  val link: String,
)
