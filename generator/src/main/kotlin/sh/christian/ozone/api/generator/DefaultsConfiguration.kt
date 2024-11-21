package sh.christian.ozone.api.generator

import java.io.Serializable

data class DefaultsConfiguration(
  val generateUnknownsForSealedTypes: Boolean,
) : Serializable
