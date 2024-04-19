package sh.christian.ozone.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import sh.christian.ozone.api.model.JsonContent

private val json = Json {
  classDiscriminator = "${'$'}type"
  ignoreUnknownKeys = true
}

fun <T : Any> KSerializer<T>.deserialize(jsonContent: JsonContent): T {
  return json.decodeFromString(this, json.encodeToString(jsonContent))
}

fun <T : Any> KSerializer<T>.serialize(value: T): JsonContent {
  return json.decodeFromString(json.encodeToString(this, value))
}
