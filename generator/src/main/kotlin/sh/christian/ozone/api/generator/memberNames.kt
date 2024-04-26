package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.MemberName
import kotlin.properties.ReadOnlyProperty

val defaultHttpClient by memberOfPackage("sh.christian.ozone.api.xrpc")

val findSubscriptionSerializer by memberOfPackage("sh.christian.ozone.api.xrpc")

val persistentListOf by memberOfPackage("kotlinx.collections.immutable")

val procedure by extensionMemberOfPackage("sh.christian.ozone.api.xrpc")

val query by extensionMemberOfPackage("sh.christian.ozone.api.xrpc")

val runBlocking by memberOfPackage("kotlinx.coroutines")

val subscription by extensionMemberOfPackage("sh.christian.ozone.api.xrpc")

val toAtpResponse by extensionMemberOfPackage("sh.christian.ozone.api.xrpc")

val toAtpResult by extensionMemberOfPackage("sh.christian.ozone.api.xrpc")

val toAtpModel by extensionMemberOfPackage("sh.christian.ozone.api.xrpc")

val toImmutableList by extensionMemberOfPackage("kotlinx.collections.immutable")

val valueClassSerializer by memberOfPackage("sh.christian.ozone.api.runtime")

val withXrpcConfiguration by extensionMemberOfPackage("sh.christian.ozone.api.xrpc")

private fun memberOfPackage(packageName: String): ReadOnlyProperty<Any?, MemberName> {
  return ReadOnlyProperty { _, property -> MemberName(packageName, property.name) }
}

private fun extensionMemberOfPackage(packageName: String): ReadOnlyProperty<Any?, MemberName> {
  return ReadOnlyProperty { _, property -> MemberName(packageName, property.name, isExtension = true) }
}
