package sh.christian.ozone.api.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

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

class BlobSerializer : JsonContentPolymorphicSerializer<Blob>(Blob::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Blob> {
    return if (element.jsonObject.containsKey("ref")) {
      Blob.StandardBlob.serializer()
    } else {
      Blob.LegacyBlob.serializer()
    }
  }
}
