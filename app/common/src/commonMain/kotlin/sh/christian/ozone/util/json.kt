package sh.christian.ozone.util

import kotlinx.serialization.KSerializer
import sh.christian.ozone.BlueskyJson
import sh.christian.ozone.api.model.JsonContent

fun <T : Any> KSerializer<T>.deserialize(jsonContent: JsonContent): T {
  return BlueskyJson.decodeFromString(this, BlueskyJson.encodeToString(jsonContent))
}

fun <T : Any> KSerializer<T>.serialize(value: T): JsonContent {
  return BlueskyJson.decodeFromString(BlueskyJson.encodeToString(this, value))
}
