package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.MemberName

val persistentListOf: MemberName =
  MemberName("kotlinx.collections.immutable", "persistentListOf")

val procedure: MemberName =
  MemberName("sh.christian.ozone.api.xrpc", "procedure", isExtension = true)

val query: MemberName =
  MemberName("sh.christian.ozone.api.xrpc", "query", isExtension = true)

val toAtpResponse: MemberName =
  MemberName("sh.christian.ozone.api.xrpc", "toAtpResponse", isExtension = true)

val toImmutableList: MemberName =
  MemberName("kotlinx.collections.immutable", "toImmutableList", isExtension = true)

val valueClassSerializer =
  MemberName("sh.christian.ozone.api.runtime", "valueClassSerializer")

val withJsonConfiguration =
  MemberName("sh.christian.ozone.api.xrpc", "withJsonConfiguration", isExtension = true)
