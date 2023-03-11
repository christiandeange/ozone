package sh.christian.ozone.api.lexicon

import com.squareup.moshi.JsonClass
import sh.christian.ozone.api.lexicon.LexiconPrimitive.Type.BOOLEAN
import sh.christian.ozone.api.lexicon.LexiconPrimitive.Type.INTEGER
import sh.christian.ozone.api.lexicon.LexiconPrimitive.Type.NUMBER
import sh.christian.ozone.api.lexicon.LexiconPrimitive.Type.STRING
import sh.christian.ozone.api.lexicon.LexiconUserType.Type.AUDIO
import sh.christian.ozone.api.lexicon.LexiconUserType.Type.BLOB
import sh.christian.ozone.api.lexicon.LexiconUserType.Type.IMAGE
import sh.christian.ozone.api.lexicon.LexiconUserType.Type.OBJECT
import sh.christian.ozone.api.lexicon.LexiconUserType.Type.PROCEDURE
import sh.christian.ozone.api.lexicon.LexiconUserType.Type.QUERY
import sh.christian.ozone.api.lexicon.LexiconUserType.Type.RECORD
import sh.christian.ozone.api.lexicon.LexiconUserType.Type.TOKEN
import sh.christian.ozone.api.lexicon.LexiconUserType.Type.VIDEO

typealias LexiconReference = String

@JsonClass(generateAdapter = true)
data class LexiconDocument(
  val lexicon: Int,
  val id: String,
  val revision: Long?,
  val description: String?,
  val defs: Map<String, LexiconDocumentElement> = emptyMap(),
) {
  init {
    require(lexicon == 1) { "Unexpected lexicon version: $lexicon" }
  }
}

sealed interface LexiconDocumentElement {
  data class UserType(
    val userType: LexiconUserType,
  ) : LexiconDocumentElement

  data class Array(
    val array: LexiconArray,
  ) : LexiconDocumentElement

  data class Primitive(
    val primitive: LexiconPrimitive,
  ) : LexiconDocumentElement

  data class ReferenceList(
    val references: List<LexiconReference> = emptyList(),
  ) : LexiconDocumentElement
}

sealed interface LexiconUserType : LexiconDocumentElement {
  val type: Type
  val description: String?

  enum class Type {
    QUERY,
    PROCEDURE,
    RECORD,
    TOKEN,
    OBJECT,
    BLOB,
    IMAGE,
    VIDEO,
    AUDIO,
    UNKNOWN,
  }
}

@JsonClass(generateAdapter = true)
data class LexiconToken(
  override val description: String?,
) : LexiconUserType {
  override val type get() = TOKEN
}

@JsonClass(generateAdapter = true)
data class LexiconObject(
  val required: List<String> = emptyList(),
  val properties: Map<String, LexiconObjectProperty> = emptyMap(),
  override val description: String?,
) : LexiconUserType {
  override val type get() = OBJECT
}

sealed interface LexiconObjectProperty {
  data class Reference(
    val reference: LexiconReference,
  ) : LexiconObjectProperty

  data class Array(
    val array: LexiconArray,
  ) : LexiconObjectProperty

  data class Primitive(
    val primitive: LexiconPrimitive,
  ) : LexiconObjectProperty

  data class ReferenceList(
    val references: List<LexiconReference> = emptyList(),
  ) : LexiconObjectProperty
}

@JsonClass(generateAdapter = true)
data class LexiconRecord(
  val key: String?,
  val record: LexiconObject,
  override val description: String?,
) : LexiconUserType {
  override val type get() = RECORD
}

@JsonClass(generateAdapter = true)
data class LexiconXrpcQuery(
  val parameters: Map<String, LexiconPrimitive> = emptyMap(),
  val output: LexiconXrpcBody?,
  val errors: List<LexiconXrpcError> = emptyList(),
  override val description: String?,
) : LexiconUserType {
  override val type get() = QUERY
}

@JsonClass(generateAdapter = true)
data class LexiconXrpcProcedure(
  val parameters: Map<String, LexiconPrimitive> = emptyMap(),
  val input: LexiconXrpcBody?,
  val output: LexiconXrpcBody?,
  val errors: List<LexiconXrpcError> = emptyList(),
  override val description: String?,
) : LexiconUserType {
  override val type get() = PROCEDURE
}

@JsonClass(generateAdapter = true)
data class LexiconXrpcBody(
  val description: String?,
  val encoding: OneOrMore<String>,
  val schema: LexiconObject,
)

@JsonClass(generateAdapter = true)
data class LexiconXrpcError(
  val name: String,
  val description: String?,
)

@JsonClass(generateAdapter = true)
data class LexiconBlob(
  val accept: List<String> = emptyList(),
  val maxSize: Long?,
  override val description: String?,
) : LexiconUserType {
  override val type get() = BLOB
}

@JsonClass(generateAdapter = true)
data class LexiconImage(
  val accept: List<String> = emptyList(),
  val maxSize: Long?,
  val maxWidth: Long?,
  val maxHeight: Long?,
  override val description: String?,
) : LexiconUserType {
  override val type get() = IMAGE
}

@JsonClass(generateAdapter = true)
data class LexiconVideo(
  val accept: List<String> = emptyList(),
  val maxSize: Long?,
  val maxWidth: Long?,
  val maxHeight: Long?,
  val maxLength: Long?,
  override val description: String?,
) : LexiconUserType {
  override val type get() = VIDEO
}

@JsonClass(generateAdapter = true)
data class LexiconAudio(
  val accept: List<String> = emptyList(),
  val maxSize: Long?,
  val maxLength: Long?,
  override val description: String?,
) : LexiconUserType {
  override val type get() = AUDIO
}

@JsonClass(generateAdapter = true)
data class LexiconArray(
  val type: String,
  val description: String?,
  val items: LexiconArrayItem,
  val minLength: Long?,
  val maxLength: Long?,
)

sealed interface LexiconArrayItem {
  data class Reference(
    val reference: LexiconReference,
  ) : LexiconArrayItem

  data class Primitive(
    val primitive: LexiconPrimitive,
  ) : LexiconArrayItem

  data class ReferenceList(
    val references: List<LexiconReference> = emptyList(),
  ) : LexiconArrayItem
}

sealed interface LexiconPrimitive {
  val type: Type
  val description: String?

  enum class Type {
    BOOLEAN,
    NUMBER,
    INTEGER,
    STRING,
    ;
  }
}

@JsonClass(generateAdapter = true)
data class LexiconBoolean(
  val default: Boolean?,
  val const: Boolean?,
  override val description: String?,
) : LexiconPrimitive {
  override val type get() = BOOLEAN
}

@JsonClass(generateAdapter = true)
data class LexiconNumber(
  val default: Long?,
  val minimum: Long?,
  val maximum: Long?,
  val enum: List<Long> = emptyList(),
  val const: Long?,
  override val description: String?,
) : LexiconPrimitive {
  override val type get() = NUMBER
}

@JsonClass(generateAdapter = true)
data class LexiconInteger(
  val default: Int?,
  val minimum: Int?,
  val maximum: Int?,
  val enum: List<Int> = emptyList(),
  val const: Int?,
  override val description: String?,
) : LexiconPrimitive {
  override val type get() = INTEGER
}

@JsonClass(generateAdapter = true)
data class LexiconString(
  val default: String?,
  val minLength: Long?,
  val maxLength: Long?,
  val enum: List<String> = emptyList(),
  val const: String?,
  val knownValues: List<String> = emptyList(),
  override val description: String?,
) : LexiconPrimitive {
  override val type get() = STRING
}
