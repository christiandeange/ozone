package sh.christian.ozone.api.lexicon

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// region Primitives

@JsonClass(generateAdapter = true)
data class LexiconBoolean(
  override val description: String?,
  val default: Boolean?,
  val const: Boolean?,
) : LexiconPrimitive

@JsonClass(generateAdapter = true)
data class LexiconFloat(
  override val description: String?,
  val default: Double?,
  val minimum: Double?,
  val maximum: Double?,
  val enum: List<Double> = emptyList(),
  val const: Double?,
) : LexiconPrimitive

@JsonClass(generateAdapter = true)
data class LexiconInteger(
  override val description: String?,
  val default: Int?,
  val minimum: Int?,
  val maximum: Int?,
  val enum: List<Int> = emptyList(),
  val const: Int?,
) : LexiconPrimitive

enum class LexiconStringFormat {
  @Json(name = "datetime") DATETIME,
  @Json(name = "uri") URI,
  @Json(name = "at-uri") AT_URI,
  @Json(name = "did") DID,
  @Json(name = "handle") HANDLE,
  @Json(name = "at-identifier") AT_IDENTIFIER,
  @Json(name = "nsid") NSID,
  @Json(name = "cid") CID,
  @Json(name = "language") LANGUAGE,
  @Json(name = "tid") TID,
  @Json(name = "record-key") RECORD_KEY,
}

@JsonClass(generateAdapter = true)
data class LexiconString(
  val format: LexiconStringFormat?,
  override val description: String?,
  val default: String?,
  val minLength: Long?,
  val maxLength: Long?,
  val minGraphemes: Long?,
  val maxGraphemes: Long?,
  val enum: List<String> = emptyList(),
  val const: String?,
  val knownValues: List<String> = emptyList(),
) : LexiconPrimitive

@JsonClass(generateAdapter = true)
data class LexiconUnknown(
  override val description: String?,
) : LexiconPrimitive

sealed interface LexiconPrimitive : LexiconUserType

// endregion

// region InterPlanetary Linked Data (IPLD)

@JsonClass(generateAdapter = true)
data class LexiconBytes(
  override val description: String?,
  val maxLength: Double?,
  val minLength: Double?,
) : LexiconIpldType

@JsonClass(generateAdapter = true)
data class LexiconCidLink(
  override val description: String?,
) : LexiconIpldType

sealed interface LexiconIpldType : LexiconUserType

// endregion

// region References

@JsonClass(generateAdapter = true)
data class LexiconSingleReference(
  override val description: String?,
  val ref: String,
) : LexiconReference

@JsonClass(generateAdapter = true)
data class LexiconUnionReference(
  override val description: String?,
  val refs: List<String>,
  val closed: Boolean?,
) : LexiconReference {
  val references: List<LexiconSingleReference>
    get() = refs.map { LexiconSingleReference(description = null, ref = it) }
}

sealed interface LexiconReference {
  val description: String?
}

// endregion

// region Blobs

@JsonClass(generateAdapter = true)
data class LexiconBlob(
  override val description: String?,
  val accept: List<String> = emptyList(),
  val maxSize: Double?,
) : LexiconUserType

// endregion

// region Complex

@JsonClass(generateAdapter = true)
data class LexiconArray(
  override val description: String?,
  val items: LexiconArrayItem,
  val minLength: Long?,
  val maxLength: Long?,
) : LexiconUserType

sealed interface LexiconArrayItem {
  data class Primitive(
    val primitive: LexiconPrimitive,
  ) : LexiconArrayItem

  data class IpldType(
    val ipld: LexiconIpldType,
  ) : LexiconArrayItem

  data class Blob(
    val blob: LexiconBlob,
  ) : LexiconArrayItem

  data class Reference(
    val reference: LexiconReference,
  ) : LexiconArrayItem
}

@JsonClass(generateAdapter = true)
data class LexiconPrimitiveArray(
  val description: String?,
  val items: LexiconPrimitive,
  val minLength: Long?,
  val maxLength: Long?,
)

@JsonClass(generateAdapter = true)
data class LexiconToken(
  override val description: String?,
) : LexiconUserType

@JsonClass(generateAdapter = true)
data class LexiconObject(
  override val description: String?,
  val required: List<String> = emptyList(),
  val nullable: List<String> = emptyList(),
  val properties: Map<String, LexiconObjectProperty> = emptyMap(),
) : LexiconUserType

sealed interface LexiconObjectProperty {
  data class Reference(
    val reference: LexiconReference,
  ) : LexiconObjectProperty

  data class IpldType(
    val ipld: LexiconIpldType,
  ) : LexiconObjectProperty

  data class Array(
    val array: LexiconArray,
  ) : LexiconObjectProperty

  data class Blob(
    val blob: LexiconBlob,
  ) : LexiconObjectProperty

  data class Primitive(
    val primitive: LexiconPrimitive,
  ) : LexiconObjectProperty
}

// endregion

// region XRPC

@JsonClass(generateAdapter = true)
data class LexiconXrpcParameters(
  val description: String?,
  val required: List<String> = emptyList(),
  val properties: Map<String, LexiconXrpcParameter> = emptyMap(),
)

sealed interface LexiconXrpcParameter {
  data class Primitive(
    val primitive: LexiconPrimitive,
  ) : LexiconXrpcParameter

  data class PrimitiveArray(
    val array: LexiconPrimitiveArray,
  ) : LexiconXrpcParameter
}

@JsonClass(generateAdapter = true)
data class LexiconXrpcBody(
  val description: String?,
  val encoding: String,
  val schema: LexiconXrpcSchemaDefinition?,
)

@JsonClass(generateAdapter = true)
data class LexiconXrpcSubscriptionMessage(
  val description: String?,
  val schema: LexiconXrpcSchemaDefinition?,
  val codes: Map<String, Long>? = emptyMap(),
)

sealed interface LexiconXrpcSchemaDefinition {
  data class Reference(
    val reference: LexiconReference,
  ) : LexiconXrpcSchemaDefinition

  data class Object(
    val value: LexiconObject,
  ) : LexiconXrpcSchemaDefinition
}

@JsonClass(generateAdapter = true)
data class LexiconXrpcError(
  val name: String,
  val description: String?,
)

@JsonClass(generateAdapter = true)
data class LexiconXrpcQuery(
  override val description: String?,
  val parameters: LexiconXrpcParameters?,
  val output: LexiconXrpcBody?,
  val errors: List<LexiconXrpcError> = emptyList(),
) : LexiconUserType

@JsonClass(generateAdapter = true)
data class LexiconXrpcProcedure(
  override val description: String?,
  val parameters: LexiconXrpcParameters?,
  val input: LexiconXrpcBody?,
  val output: LexiconXrpcBody?,
  val errors: List<LexiconXrpcError> = emptyList(),
) : LexiconUserType

@JsonClass(generateAdapter = true)
data class LexiconXrpcSubscription(
  override val description: String?,
  val parameters: LexiconXrpcParameters?,
  val message: LexiconXrpcSubscriptionMessage?,
  val errors: List<LexiconXrpcError> = emptyList(),
) : LexiconUserType

@JsonClass(generateAdapter = true)
data class LexiconRecord(
  override val description: String?,
  val key: String?,
  val record: LexiconObject,
) : LexiconUserType

// endregion

// region Core

sealed interface LexiconUserType {
  val description: String?
}

@JsonClass(generateAdapter = true)
data class LexiconDocument(
  val lexicon: Int,
  val id: String,
  val revision: Double?,
  val description: String?,
  val defs: Map<String, LexiconUserType> = emptyMap(),
) {
  init {
    require(lexicon == 1) { "Unexpected lexicon version: $lexicon" }

    // TODO: Parse `id` against NSID grammar https://atproto.com/specs/nsid

    defs.forEach { (key, value) ->
      when (value) {
        is LexiconRecord,
        is LexiconXrpcProcedure,
        is LexiconXrpcQuery,
        is LexiconXrpcSubscription -> {
          require(key == "main") {
            "Records, procedures, queries, and subscriptions must be the main definition."
          }
        }
        else -> Unit
      }
    }
  }
}

@JsonClass(generateAdapter = true)
data class LexiconDocumentMetadata(
  val lexicon: Int,
  val id: String,
  val revision: Double?,
  val description: String?,
) {
  init {
    require(lexicon == 1) { "Unexpected lexicon version: $lexicon" }

    // TODO: Parse `id` against NSID grammar https://atproto.com/specs/nsid
  }
}

// endregion
