package sh.christian.ozone.api.generator

import java.io.Serializable

sealed interface ApiConfiguration : Serializable {
  object None : ApiConfiguration

  data class GenerateApiConfiguration(
    val packageName: String,
    val interfaceName: String,
    val implementationName: String?,
    val suspending: Boolean,
    val returnType: ApiReturnType,
  ) : ApiConfiguration
}
