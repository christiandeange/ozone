package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.ClassName
import kotlin.properties.ReadOnlyProperty

object TypeNames {
  val AtIdentifier by classOfPackage("sh.christian.ozone.api")
  val AtUri by classOfPackage("sh.christian.ozone.api")
  val AtpResponse by classOfPackage("sh.christian.ozone.api.response")
  val Cid by classOfPackage("sh.christian.ozone.api")
  val Did by classOfPackage("sh.christian.ozone.api")
  val Handle by classOfPackage("sh.christian.ozone.api")
  val HttpClient by classOfPackage("io.ktor.client")
  val ImmutableList by classOfPackage("kotlinx.collections.immutable")
  val ImmutableListSerializer by classOfPackage("sh.christian.ozone.api.runtime")
  val Instant by classOfPackage("kotlinx.datetime")
  val JsonElement by classOfPackage("kotlinx.serialization.json")
  val KSerializer by classOfPackage("kotlinx.serialization")
  val Language by classOfPackage("sh.christian.ozone.api")
  val Nsid by classOfPackage("sh.christian.ozone.api")
  val Result by classOfPackage("kotlin")
  val SerialName by classOfPackage("kotlinx.serialization")
  val Serializable by classOfPackage("kotlinx.serialization")
  val Uri by classOfPackage("sh.christian.ozone.api")
}

private fun classOfPackage(packageName: String): ReadOnlyProperty<Any?, ClassName> {
  return ReadOnlyProperty { _, property -> ClassName(packageName, property.name) }
}
