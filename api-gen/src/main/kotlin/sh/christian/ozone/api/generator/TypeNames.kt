package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.ClassName
import kotlin.properties.ReadOnlyProperty

object TypeNames {
  val AtpResponse by classOfPackage("sh.christian.ozone.api.response")
  val Encoding by classOfPackage("sh.christian.ozone.api.runtime")
  val HttpClient by classOfPackage("io.ktor.client")
  val ImmutableList by classOfPackage("kotlinx.collections.immutable")
  val ImmutableListSerializer by classOfPackage("sh.christian.ozone.api.runtime")
  val Instant by classOfPackage("kotlinx.datetime")
  val JsonElement by classOfPackage("kotlinx.serialization.json")
  val KSerializer by classOfPackage("kotlinx.serialization")
  val Serializable by classOfPackage("kotlinx.serialization")
  val SerialName by classOfPackage("kotlinx.serialization")
}

private fun classOfPackage(packageName: String): ReadOnlyProperty<Any?, ClassName> {
  return ReadOnlyProperty { _, property -> ClassName(packageName, property.name) }
}
