@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package sh.christian.ozone.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import sh.christian.ozone.api.runtime.JsonContentSerializer

@Serializable(with = JsonContentSerializer::class)
actual data class JsonContent(
  val value: JsonElement,
)
