package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.TypeName

data class SimpleProperty(
  val name: String,
  val type: TypeName,
  val nullable: Boolean,
) {
  override fun toString(): String {
    return "SimpleProperty(name='$name', type=$type, nullable=$nullable)"
  }
}
