package sh.christian.ozone.api.generator

import java.io.Serializable

sealed interface ApiConfiguration : Serializable {
  object None : ApiConfiguration

  data class GenerateApiConfiguration(
    val interfaceName: String,
    val returnType: ApiReturnType,
  ) : ApiConfiguration
}
