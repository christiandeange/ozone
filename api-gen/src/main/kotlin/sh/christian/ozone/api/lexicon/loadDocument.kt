@file:OptIn(ExperimentalStdlibApi::class)

package sh.christian.ozone.api.lexicon

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token.*
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapter
import com.squareup.moshi.addAdapter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

fun loadDocument(json: String): LexiconDocument {
  return moshi.adapter<LexiconDocument>().fromJson(json)!!
}

private val moshi: Moshi = Moshi.Builder()
  .addAdapter(LexiconDocumentElementAdapter())
  .addAdapter(LexiconUserTypeAdapter())
  .addAdapter(LexiconObjectPropertyAdapter())
  .addAdapter(LexiconArrayItemAdapter())
  .addAdapter(LexiconPrimitiveAdapter())
  .add(OneOrMoreAdapterFactory())
  .add(LexiconCodegenAdapterFactory())
  .build()

private class LexiconCodegenAdapterFactory : JsonAdapter.Factory {
  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
  ): JsonAdapter<*>? = when (type) {
    LexiconArray::class.java -> LexiconArrayJsonAdapter(moshi)
    LexiconAudio::class.java -> LexiconAudioJsonAdapter(moshi)
    LexiconBlob::class.java -> LexiconBlobJsonAdapter(moshi)
    LexiconBoolean::class.java -> LexiconBooleanJsonAdapter(moshi)
    LexiconDocument::class.java -> LexiconDocumentJsonAdapter(moshi)
    LexiconImage::class.java -> LexiconImageJsonAdapter(moshi)
    LexiconInteger::class.java -> LexiconIntegerJsonAdapter(moshi)
    LexiconNumber::class.java -> LexiconNumberJsonAdapter(moshi)
    LexiconObject::class.java -> LexiconObjectJsonAdapter(moshi)
    LexiconRecord::class.java -> LexiconRecordJsonAdapter(moshi)
    LexiconString::class.java -> LexiconStringJsonAdapter(moshi)
    LexiconToken::class.java -> LexiconTokenJsonAdapter(moshi)
    LexiconVideo::class.java -> LexiconVideoJsonAdapter(moshi)
    LexiconXrpcBody::class.java -> LexiconXrpcBodyJsonAdapter(moshi)
    LexiconXrpcError::class.java -> LexiconXrpcErrorJsonAdapter(moshi)
    LexiconXrpcProcedure::class.java -> LexiconXrpcProcedureJsonAdapter(moshi)
    LexiconXrpcQuery::class.java -> LexiconXrpcQueryJsonAdapter(moshi)
    else -> null
  }
}

private abstract class Deserializer<T : Any> : JsonAdapter<T>() {
  override fun toJson(
    writer: JsonWriter,
    value: T?,
  ): Unit = error("Serialization not supported.")
}

private class LexiconDocumentElementAdapter : Deserializer<LexiconDocumentElement>() {
  override fun fromJson(reader: JsonReader): LexiconDocumentElement {
    return when (reader.peek()) {
      BEGIN_ARRAY -> moshi.adapter<LexiconDocumentElement.ReferenceList>().fromJson(reader)!!
      BEGIN_OBJECT -> {
        val type = reader.peekType()
        reader.maybeReadLexiconArray(type)?.let { LexiconDocumentElement.Array(it) }
          ?: reader.maybeReadLexiconPrimitive(type)?.let { LexiconDocumentElement.Primitive(it) }
          ?: reader.maybeReadLexiconUserType(type)?.let { LexiconDocumentElement.UserType(it) }
          ?: reader.fail("Unexpected type '$type'")
      }

      else -> reader.fail()
    }
  }
}

private class LexiconUserTypeAdapter : Deserializer<LexiconUserType>() {
  override fun fromJson(reader: JsonReader): LexiconUserType {
    val type = reader.peekType()
    return reader.maybeReadLexiconUserType(type)
      ?: reader.fail("Unexpected type '$type'")
  }
}

private class LexiconObjectPropertyAdapter : Deserializer<LexiconObjectProperty>() {
  override fun fromJson(reader: JsonReader): LexiconObjectProperty {
    return when (reader.peek()) {
      BEGIN_ARRAY -> moshi.adapter<LexiconObjectProperty.ReferenceList>().fromJson(reader)!!
      STRING -> LexiconObjectProperty.Reference(reader.nextString())
      BEGIN_OBJECT -> {
        val type = reader.peekType()
        reader.maybeReadLexiconArray(type)?.let { LexiconObjectProperty.Array(it) }
          ?: reader.maybeReadLexiconPrimitive(type)?.let { LexiconObjectProperty.Primitive(it) }
          ?: reader.fail("Unexpected type '$type'")
      }

      else -> reader.fail()
    }
  }
}

private class LexiconArrayItemAdapter : Deserializer<LexiconArrayItem>() {
  override fun fromJson(reader: JsonReader): LexiconArrayItem {
    return when (reader.peek()) {
      BEGIN_ARRAY -> moshi.adapter<LexiconArrayItem.ReferenceList>().fromJson(reader)!!
      STRING -> LexiconArrayItem.Reference(reader.nextString())
      BEGIN_OBJECT -> {
        val type = reader.peekType()
        reader.maybeReadLexiconPrimitive(type)?.let { LexiconArrayItem.Primitive(it) }
          ?: reader.fail("Unexpected type '$type'")
      }

      else -> reader.fail()
    }
  }
}

private class LexiconPrimitiveAdapter : Deserializer<LexiconPrimitive>() {
  override fun fromJson(reader: JsonReader): LexiconPrimitive {
    val type = reader.peekType()
    return reader.maybeReadLexiconPrimitive(type)
      ?: reader.fail("Unexpected type '$type'")
  }
}

private fun JsonReader.maybeReadLexiconArray(type: String): LexiconArray? {
  return when (type) {
    "array" -> moshi.adapter<LexiconArray>().fromJson(this)!!
    else -> null
  }
}

private fun JsonReader.maybeReadLexiconPrimitive(type: String): LexiconPrimitive? {
  return when (type) {
    "boolean" -> moshi.adapter<LexiconBoolean>().fromJson(this)!!
    "number" -> moshi.adapter<LexiconNumber>().fromJson(this)!!
    "integer" -> moshi.adapter<LexiconInteger>().fromJson(this)!!
    "string" -> moshi.adapter<LexiconString>().fromJson(this)!!
    else -> null
  }
}

private fun JsonReader.maybeReadLexiconUserType(type: String): LexiconUserType? {
  return when (type) {
    "query" -> moshi.adapter<LexiconXrpcQuery>().fromJson(this)!!
    "procedure" -> moshi.adapter<LexiconXrpcProcedure>().fromJson(this)!!
    "record" -> moshi.adapter<LexiconRecord>().fromJson(this)!!
    "token" -> moshi.adapter<LexiconToken>().fromJson(this)!!
    "object" -> moshi.adapter<LexiconObject>().fromJson(this)!!
    "blob" -> moshi.adapter<LexiconBlob>().fromJson(this)!!
    "image" -> moshi.adapter<LexiconImage>().fromJson(this)!!
    "video" -> moshi.adapter<LexiconVideo>().fromJson(this)!!
    "audio" -> moshi.adapter<LexiconAudio>().fromJson(this)!!
    else -> null
  }
}

private class OneOrMoreAdapterFactory : JsonAdapter.Factory {
  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
  ): JsonAdapter<*>? {
    val genericType = type as? ParameterizedType ?: return null
    if (genericType.rawType != OneOrMore::class.java) return null

    val wrappedType = genericType.actualTypeArguments.first()
    return OneOrMoreAdapter<Any>(wrappedType)
  }
}

private class OneOrMoreAdapter<T : Any>(
  private val wrappedType: Type,
) : Deserializer<OneOrMore<T>>() {
  override fun fromJson(reader: JsonReader): OneOrMore<T> {
    return when (reader.peek()) {
      BEGIN_ARRAY -> {
        val listType = Types.newParameterizedType(List::class.java, wrappedType)
        OneOrMore.More(moshi.adapter<List<T>>(listType).fromJson(reader)!!)
      }

      BEGIN_OBJECT,
      STRING,
      NUMBER,
      BOOLEAN -> {
        OneOrMore.One(moshi.adapter<T>(wrappedType).fromJson(reader)!!)
      }

      NAME,
      NULL,
      END_ARRAY,
      END_OBJECT,
      END_DOCUMENT,
      null -> reader.fail()
    }
  }
}

private fun JsonReader.peekType(): String {
  if (peek() != BEGIN_OBJECT) fail()

  val jsonObject = peekJson().readJsonValue() as Map<*, *>
  return jsonObject["type"]?.toString().orEmpty()
}

private fun JsonReader.fail(message: String = "Unexpected input"): Nothing {
  error("$message: ${peek()} at $path")
}
