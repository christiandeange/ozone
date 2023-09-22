package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BOOLEAN_ARRAY
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.CHAR_ARRAY
import com.squareup.kotlinpoet.COLLECTION
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.DOUBLE_ARRAY
import com.squareup.kotlinpoet.Dynamic
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FLOAT_ARRAY
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.INT_ARRAY
import com.squareup.kotlinpoet.ITERABLE
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LONG_ARRAY
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MUTABLE_COLLECTION
import com.squareup.kotlinpoet.MUTABLE_ITERABLE
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.MUTABLE_SET
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.SHORT_ARRAY
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.U_BYTE
import com.squareup.kotlinpoet.U_BYTE_ARRAY
import com.squareup.kotlinpoet.U_INT
import com.squareup.kotlinpoet.U_INT_ARRAY
import com.squareup.kotlinpoet.U_LONG
import com.squareup.kotlinpoet.U_LONG_ARRAY
import com.squareup.kotlinpoet.U_SHORT
import com.squareup.kotlinpoet.U_SHORT_ARRAY
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import sh.christian.ozone.api.generator.TypeNames
import sh.christian.ozone.api.generator.persistentListOf

data class SimpleProperty(
  val name: String,
  val type: TypeName,
  val nullable: Boolean,
  val description: String?,
  val definedDefault: CodeBlock?,
  val requirements: List<Requirement>,
) {
  fun defaultValue(): CodeBlock = definedDefault ?: type.defaultValue(nullable)

  override fun toString(): String {
    return "SimpleProperty(name='$name', type=$type, nullable=$nullable, description=$description)"
  }
}

private fun TypeName.defaultValue(nullable: Boolean): CodeBlock = buildCodeBlock {
  when (this@defaultValue) {
    is ClassName -> {
      if (nullable) {
        add("null")
      } else {
        when (this@defaultValue) {
          BOOLEAN -> add("false")
          BYTE -> add("0")
          SHORT -> add("0")
          INT -> add("0")
          LONG -> add("0L")
          U_BYTE -> add("0u")
          U_SHORT -> add("0u")
          U_INT -> add("0u")
          U_LONG -> add("0uL")
          CHAR -> add("Char(0)")
          FLOAT -> add("0f")
          DOUBLE -> add("0.0")
          STRING -> add("\"\"")
          TypeNames.AtIdentifier,
          TypeNames.AtUri,
          TypeNames.Cid,
          TypeNames.Did,
          TypeNames.Handle,
          TypeNames.Language,
          TypeNames.Nsid,
          TypeNames.Uri -> {
            add("%T(\"\")", this@defaultValue)
          }
          else -> error("Unable to provide non-null default for ClassName: $this")
        }
      }
    }
    is Dynamic -> add("dynamic")
    is LambdaTypeName -> {
      val parametersList = parameters.joinToString(", ") { "_" }
      val defaultReturnValue = returnType.defaultValue(returnType.isNullable)

      beginControlFlow("{ $parametersList -> ")
      add(defaultReturnValue)
      endControlFlow()
    }
    is ParameterizedTypeName -> {
      when (rawType) {
        ARRAY -> add("emptyArray()")
        BOOLEAN_ARRAY -> add("booleanArrayOf()")
        BYTE_ARRAY -> add("byteArrayOf()")
        CHAR_ARRAY -> add("charArrayOf()")
        SHORT_ARRAY -> add("shortArrayOf()")
        INT_ARRAY -> add("intArrayOf()")
        LONG_ARRAY -> add("longArrayOf()")
        FLOAT_ARRAY -> add("floatArrayOf()")
        DOUBLE_ARRAY -> add("doubleArrayOf()")
        U_BYTE_ARRAY -> add("ubyteArrayOf()")
        U_SHORT_ARRAY -> add("ushortArrayOf()")
        U_INT_ARRAY -> add("uintArrayOf()")
        U_LONG_ARRAY -> add("ulongArrayOf()")
        ITERABLE -> add("emptyList()")
        COLLECTION -> add("emptyList()")
        LIST -> add("emptyList()")
        SET -> add("emptySet()")
        MAP -> add("emptyMap()")
        MUTABLE_ITERABLE -> add("mutableListOf()")
        MUTABLE_COLLECTION -> add("mutableListOf()")
        MUTABLE_LIST -> add("mutableListOf()")
        MUTABLE_SET -> add("mutableSetOf()")
        MUTABLE_MAP -> add("mutableMapOf()")
        TypeNames.ReadOnlyList -> add("%M()", persistentListOf)
        else -> error("Unable to provide non-null default for ParameterizedTypeName: $this")
      }
    }

    is TypeVariableName -> {
      if (nullable) {
        add("null")
      } else {
        error("Unable to provide non-null default for TypeVariableName: $this")
      }
    }

    is WildcardTypeName -> {
      if (nullable) {
        add("null")
      } else {
        error("TODO: Default value for WildcardTypeName: $this")
      }
    }
  }
}
