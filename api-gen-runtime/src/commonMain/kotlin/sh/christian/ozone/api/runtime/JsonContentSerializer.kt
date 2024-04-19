@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package sh.christian.ozone.api.runtime

import kotlinx.serialization.KSerializer
import sh.christian.ozone.api.model.JsonContent

expect object JsonContentSerializer : KSerializer<JsonContent>
