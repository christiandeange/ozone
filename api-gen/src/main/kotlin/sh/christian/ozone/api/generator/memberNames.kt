package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.MemberName

val persistentListOf: MemberName =
  MemberName("kotlinx.collections.immutable", "persistentListOf")

val toImmutableList: MemberName =
  MemberName("kotlinx.collections.immutable", "toImmutableList", isExtension = true)

val valueClassSerializer =
  MemberName("sh.christian.ozone.api.runtime", "valueClassSerializer")
