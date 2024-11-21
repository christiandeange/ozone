package sh.christian.ozone.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import sh.christian.ozone.api.runtime.JsonContentSerializer

/**
 * Represents arbitrary JSON content that corresponds to a model object.
 * This is often used to represent content that does not have known type defined at runtime.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@Serializable(with = JsonContentSerializer::class)
expect class JsonContent {

  /**
   * Decode the content as a known [@Serializable][Serializable] class.
   */
  inline fun <reified T : Any> decodeAs(): T

  companion object {
    /**
     * Encode the value as a [JsonContent] object.
     */
    inline fun <reified T : Any> Json.encodeAsJsonContent(value: T): JsonContent
  }
}
