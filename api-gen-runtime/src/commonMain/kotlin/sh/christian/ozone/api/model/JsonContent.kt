package sh.christian.ozone.api.model

import kotlinx.serialization.Serializable
import sh.christian.ozone.api.runtime.JsonContentSerializer

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@Serializable(with = JsonContentSerializer::class)
expect class JsonContent
