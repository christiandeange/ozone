package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.TypeName

data class SimpleProperty(
  val name: String,
  val type: TypeName,
) {
  override fun toString(): String {
    return "SimpleProperty(name='$name', type=$type)"
  }
}