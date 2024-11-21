@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package sh.christian.ozone.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import sh.christian.ozone.api.runtime.JsonContentSerializer

@Serializable(with = JsonContentSerializer::class)
actual data class JsonContent(
  val value: JsonElement,
  val format: Json,
) {
  actual inline fun <reified T : Any> decodeAs(): T {
    return format.decodeFromJsonElement(value)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as JsonContent

    return value == other.value
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }

  override fun toString(): String {
    return "JsonContent('$value')"
  }

  actual companion object {
    actual inline fun <reified T : Any> Json.encodeAsJsonContent(value: T): JsonContent {
      return JsonContent(encodeToJsonElement(value), this)
    }
  }
}
