@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package sh.christian.ozone.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import sh.christian.ozone.api.runtime.BlueskyJson
import sh.christian.ozone.api.runtime.JsonContentSerializer

@Serializable(with = JsonContentSerializer::class)
actual data class JsonContent(
  val value: String,
) {
  actual inline fun <reified T : Any> decodeAs(): T {
    return BlueskyJson.decodeFromString(value)
  }

  actual companion object {
    actual inline fun <reified T : Any> encodeFrom(value: T): JsonContent {
      return JsonContent(BlueskyJson.encodeToString(value))
    }
  }
}
