package sh.christian.ozone.api.generator

import java.io.Serializable

data class ApiConfiguration(
  val packageName: String,
  val interfaceName: String,
  val implementationName: String?,
  val suspending: Boolean,
  val returnType: ApiReturnType,
  val includeMethods: List<Regex>,
  val excludeMethods: List<Regex>,
) : Serializable
