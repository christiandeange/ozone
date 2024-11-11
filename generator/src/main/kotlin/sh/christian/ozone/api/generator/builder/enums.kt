package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.ClassName

data class EnumClass(
  val className: ClassName,
  val description: String?,
)

data class EnumEntry(
  val name: String,
  val description: String?,
)
