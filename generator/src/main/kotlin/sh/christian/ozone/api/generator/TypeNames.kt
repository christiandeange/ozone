package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.ClassName
import kotlin.properties.ReadOnlyProperty

object TypeNames {
  val AtIdentifier by classOfPackage("sh.christian.ozone.api")
  val AtUri by classOfPackage("sh.christian.ozone.api")
  val AtpResponse by classOfPackage("sh.christian.ozone.api.response")
  val Blob by classOfPackage("sh.christian.ozone.api.model")
  val ByteString by classOfPackage("kotlinx.serialization.cbor")
  val Cid by classOfPackage("sh.christian.ozone.api")
  val Deprecated by classOfPackage("kotlin")
  val Did by classOfPackage("sh.christian.ozone.api")
  val Flow by classOfPackage("kotlinx.coroutines.flow")
  val Handle by classOfPackage("sh.christian.ozone.api")
  val HttpClient by classOfPackage("io.ktor.client")
  val JsonElement by classOfPackage("kotlinx.serialization.json")
  val KSerializer by classOfPackage("kotlinx.serialization")
  val Language by classOfPackage("sh.christian.ozone.api")
  val Nsid by classOfPackage("sh.christian.ozone.api")
  val ReadOnlyList by classOfPackage("sh.christian.ozone.api.model")
  val Result by classOfPackage("kotlin")
  val SerialName by classOfPackage("kotlinx.serialization")
  val Serializable by classOfPackage("kotlinx.serialization")
  val Timestamp by classOfPackage("sh.christian.ozone.api.model")
  val Uri by classOfPackage("sh.christian.ozone.api")
}

private fun classOfPackage(packageName: String): ReadOnlyProperty<Any?, ClassName> {
  return ReadOnlyProperty { _, property -> ClassName(packageName, property.name) }
}
