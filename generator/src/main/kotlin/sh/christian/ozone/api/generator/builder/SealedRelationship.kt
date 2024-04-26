package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.ClassName

data class SealedRelationship(
  val sealedInterface: ClassName,
  val childClass: ClassName,
  val childClassSerialName: String,
)
