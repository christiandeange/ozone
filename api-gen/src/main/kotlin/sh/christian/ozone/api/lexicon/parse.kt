@file:OptIn(ExperimentalStdlibApi::class)

package sh.christian.ozone.api.lexicon

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token.*
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.addAdapter
import java.lang.reflect.Type
import kotlin.reflect.KClass

fun parseDocument(json: String): LexiconDocument {
  return moshi.adapter<LexiconDocument>().fromJson(json)!!
}

fun parseDocumentMetadata(json: String): LexiconDocumentMetadata {
  return moshi.adapter<LexiconDocumentMetadata>().fromJson(json)!!
}

private val moshi: Moshi = Moshi.Builder()
  .add(LexiconCodegenAdapterFactory())
  .addAdapter(LexiconPrimitiveAdapter())
  .addAdapter(LexiconReferenceAdapter())
  .addAdapter(LexiconBlobAdapter())
  .addAdapter(LexiconArrayItemAdapter())
  .addAdapter(LexiconXrpcParameterAdapter())
  .addAdapter(LexiconObjectPropertyAdapter())
  .addAdapter(LexiconXrpcSchemaDefinitionAdapter())
  .addAdapter(LexiconUserTypeAdapter())
  .build()

private class LexiconCodegenAdapterFactory : JsonAdapter.Factory {
  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
  ): JsonAdapter<*>? = when (type) {
    LexiconBoolean::class -> LexiconBooleanJsonAdapter(moshi)
    LexiconNumber::class -> LexiconNumberJsonAdapter(moshi)
    LexiconInteger::class -> LexiconIntegerJsonAdapter(moshi)
    LexiconString::class -> LexiconStringJsonAdapter(moshi)
    LexiconDatetime::class -> LexiconDatetimeJsonAdapter(moshi)
    LexiconUnknown::class -> LexiconUnknownJsonAdapter(moshi)
    LexiconSingleReference::class -> LexiconSingleReferenceJsonAdapter(moshi)
    LexiconUnionReference::class -> LexiconUnionReferenceJsonAdapter(moshi)
    LexiconBlobObject::class -> LexiconBlobObjectJsonAdapter(moshi)
    LexiconImage::class -> LexiconImageJsonAdapter(moshi)
    LexiconVideo::class -> LexiconVideoJsonAdapter(moshi)
    LexiconAudio::class -> LexiconAudioJsonAdapter(moshi)
    LexiconArray::class -> LexiconArrayJsonAdapter(moshi)
    LexiconPrimitiveArray::class -> LexiconPrimitiveArrayJsonAdapter(moshi)
    LexiconToken::class -> LexiconTokenJsonAdapter(moshi)
    LexiconObject::class -> LexiconObjectJsonAdapter(moshi)
    LexiconXrpcParameters::class -> LexiconXrpcParametersJsonAdapter(moshi)
    LexiconXrpcBody::class -> LexiconXrpcBodyJsonAdapter(moshi)
    LexiconXrpcSubscriptionMessage::class -> LexiconXrpcSubscriptionMessageJsonAdapter(moshi)
    LexiconXrpcError::class -> LexiconXrpcErrorJsonAdapter(moshi)
    LexiconXrpcQuery::class -> LexiconXrpcQueryJsonAdapter(moshi)
    LexiconXrpcProcedure::class -> LexiconXrpcProcedureJsonAdapter(moshi)
    LexiconXrpcSubscription::class -> LexiconXrpcSubscriptionJsonAdapter(moshi)
    LexiconRecord::class -> LexiconRecordJsonAdapter(moshi)
    LexiconDocument::class -> LexiconDocumentJsonAdapter(moshi)
    LexiconDocumentMetadata::class -> LexiconDocumentMetadataJsonAdapter(moshi)
    else -> null
  }
}

private abstract class Deserializer<T : Any> : JsonAdapter<T>() {
  override fun toJson(
    writer: JsonWriter,
    value: T?,
  ): Unit = error("Serialization not supported.")
}

// region Custom Adapters

private class LexiconPrimitiveAdapter : Deserializer<LexiconPrimitive>() {
  override fun fromJson(reader: JsonReader): LexiconPrimitive {
    return reader.maybeReadLexiconPrimitive()
      ?: reader.failUnknownType()
  }
}

private class LexiconReferenceAdapter : Deserializer<LexiconReference>() {
  override fun fromJson(reader: JsonReader): LexiconReference {
    return reader.maybeReadLexiconReference()
      ?: reader.failUnknownType()
  }
}

private class LexiconBlobAdapter : Deserializer<LexiconBlob>() {
  override fun fromJson(reader: JsonReader): LexiconBlob {
    return reader.maybeReadLexiconBlob()
      ?: reader.failUnknownType()
  }
}

private class LexiconArrayItemAdapter : Deserializer<LexiconArrayItem>() {
  override fun fromJson(reader: JsonReader): LexiconArrayItem {
    return reader.maybeReadLexiconPrimitive()?.let(LexiconArrayItem::Primitive)
      ?: reader.maybeReadLexiconBlob()?.let(LexiconArrayItem::Blob)
      ?: reader.maybeReadLexiconReference()?.let(LexiconArrayItem::Reference)
      ?: reader.failUnknownType()
  }
}

private class LexiconXrpcParameterAdapter : Deserializer<LexiconXrpcParameter>() {
  override fun fromJson(reader: JsonReader): LexiconXrpcParameter {
    return reader.maybeReadLexiconPrimitive()?.let(LexiconXrpcParameter::Primitive)
      ?: reader.maybeReadLexiconPrimitiveArray()?.let(LexiconXrpcParameter::PrimitiveArray)
      ?: reader.failUnknownType()
  }
}

private class LexiconObjectPropertyAdapter : Deserializer<LexiconObjectProperty>() {
  override fun fromJson(reader: JsonReader): LexiconObjectProperty {
    return reader.maybeReadLexiconReference()?.let(LexiconObjectProperty::Reference)
      ?: reader.maybeReadLexiconArray()?.let(LexiconObjectProperty::Array)
      ?: reader.maybeReadLexiconBlob()?.let(LexiconObjectProperty::Blob)
      ?: reader.maybeReadLexiconPrimitive()?.let(LexiconObjectProperty::Primitive)
      ?: reader.failUnknownType()
  }
}

private class LexiconXrpcSchemaDefinitionAdapter : Deserializer<LexiconXrpcSchemaDefinition>() {
  override fun fromJson(reader: JsonReader): LexiconXrpcSchemaDefinition {
    return reader.maybeReadLexiconReference()?.let(LexiconXrpcSchemaDefinition::Reference)
      ?: reader.maybeReadLexiconObject()?.let(LexiconXrpcSchemaDefinition::Object)
      ?: reader.failUnknownType()
  }
}

private class LexiconUserTypeAdapter : Deserializer<LexiconUserType>() {
  override fun fromJson(reader: JsonReader): LexiconUserType {
    return reader.maybeReadLexiconUserType()
      ?: reader.failUnknownType()
  }
}

// endregion

// region Parsing Helpers

private fun JsonReader.maybeReadLexiconPrimitive() = maybeReadType(
  "boolean" to LexiconBoolean::class,
  "number" to LexiconNumber::class,
  "integer" to LexiconInteger::class,
  "string" to LexiconString::class,
  "datetime" to LexiconDatetime::class,
  "unknown" to LexiconUnknown::class,
)

private fun JsonReader.maybeReadLexiconReference() = maybeReadType(
  "ref" to LexiconSingleReference::class,
  "union" to LexiconUnionReference::class,
)

private fun JsonReader.maybeReadLexiconBlob() = maybeReadType(
  "blob" to LexiconBlobObject::class,
  "image" to LexiconImage::class,
  "video" to LexiconVideo::class,
  "audio" to LexiconAudio::class,
)

private fun JsonReader.maybeReadLexiconArray() = maybeReadType(
  "array" to LexiconArray::class,
)

private fun JsonReader.maybeReadLexiconPrimitiveArray() = maybeReadType(
  "array" to LexiconPrimitiveArray::class,
)

private fun JsonReader.maybeReadLexiconObject() = maybeReadType(
  "object" to LexiconObject::class,
)

private fun JsonReader.maybeReadLexiconUserType() =
  maybeReadLexiconBlob()
    ?: maybeReadLexiconPrimitive()
    ?: maybeReadLexiconArray()
    ?: maybeReadLexiconObject()
    ?: maybeReadType(
      "query" to LexiconXrpcQuery::class,
      "procedure" to LexiconXrpcProcedure::class,
      "subscription" to LexiconXrpcSubscription::class,
      "record" to LexiconRecord::class,
      "token" to LexiconToken::class,
    )

private fun <T : Any> JsonReader.maybeReadType(vararg mapping: Pair<String, KClass<out T>>): T? {
  val type = peekType()

  return mapping.toMap()[type]?.let { knownSubtype ->
    moshi.adapter(knownSubtype.java).fromJson(this)!!
  }
}

// endregion

// region JsonReader extensions

private fun JsonReader.peekType(): String {
  if (peek() != BEGIN_OBJECT) fail("Could not peek type on non-object element")

  val jsonObject = peekJson().readJsonValue() as Map<*, *>
  return jsonObject["type"]?.toString().orEmpty()
}

private fun JsonReader.fail(message: String = "Unexpected input"): Nothing {
  error("$message: ${peek()} at $path")
}

private fun JsonReader.failUnknownType(): Nothing {
  fail("Unexpected type '${peekType()}")
}

// endregion
