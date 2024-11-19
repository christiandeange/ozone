@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package sh.christian.ozone.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import sh.christian.ozone.api.runtime.BlueskyJson
import sh.christian.ozone.api.runtime.JsonContentSerializer

@Serializable(with = JsonContentSerializer::class)
actual data class JsonContent(
  val value: JsonElement,
) {
  actual inline fun <reified T : Any> decodeAs(): T {
    return BlueskyJson.decodeFromJsonElement(value)
  }

  actual companion object {
    actual inline fun <reified T : Any> encodeFrom(value: T): JsonContent {
      return JsonContent(BlueskyJson.encodeToJsonElement(value))
    }
  }
}
