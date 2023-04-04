package sh.christian.ozone.api.lexicon

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

class EnumJsonAdapterFactory<T : Enum<T>> : JsonAdapter.Factory {
  override fun create(
    type: Type,
    annotations: Set<Annotation>,
    moshi: Moshi,
  ): JsonAdapter<*>? {
    if (annotations.isNotEmpty()) {
      return null // Annotations? This factory doesn't apply.
    }

    if (type !is Class<*> || !type.isEnum) {
      return null // Not enum type? This factory doesn't apply.
    }

    return EnumIntSafeJsonAdapter(type as Class<T>).nullSafe()
  }
}

internal class EnumIntSafeJsonAdapter<T : Enum<T>>(
  private val enumType: Class<T>,
) : JsonAdapter<T>() {
  private val nameStrings: Array<String?>
  private val constants: Array<T>
  private val options: JsonReader.Options

  override fun fromJson(reader: JsonReader): T {
    val name = reader.nextString()
    val index = options.strings().indexOf(name)
    if (index != -1) return constants[index]

    // We can consume the string safely, we are terminating anyway.
    val path = reader.path
    throw JsonDataException(
      "Expected one of " + listOf(*nameStrings) + " but was " + name + " at path " + path
    )
  }

  override fun toJson(
    writer: JsonWriter,
    value: T?
  ) {
    writer.value(nameStrings[value!!.ordinal])
  }

  override fun toString(): String = "JsonAdapter(" + enumType.name + ")"

  init {
    try {
      constants = enumType.enumConstants!!
      nameStrings = arrayOfNulls(constants.size)
      for (i in constants.indices) {
        val constant = constants[i]
        val annotation = enumType.getField(constant.name).getAnnotation(Json::class.java)
        val name = annotation?.name ?: constant.name
        nameStrings[i] = name
      }
      options = JsonReader.Options.of(*nameStrings)
    } catch (e: NoSuchFieldException) {
      throw AssertionError("Missing field in " + enumType.name, e)
    }
  }
}
