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

data class SimpleProperty(
  val name: String,
  val type: TypeName,
  val nullable: Boolean,
) {
  fun defaultValue(): String = type.defaultValue(nullable)

  override fun toString(): String {
    return "SimpleProperty(name='$name', type=$type, nullable=$nullable)"
  }
}

private fun TypeName.defaultValue(nullable: Boolean): String = when (this) {
  is ClassName -> {
    if (nullable) {
      "null"
    } else {
      when (this) {
        BOOLEAN -> "false"
        BYTE -> "0"
        SHORT -> "0"
        INT -> "0"
        LONG -> "0L"
        U_BYTE -> "0u"
        U_SHORT -> "0u"
        U_INT -> "0u"
        U_LONG -> "0uL"
        CHAR -> "Char(0)"
        FLOAT -> "0f"
        DOUBLE -> "0.0"
        STRING -> "\"\""
        else -> error("Unable to provide non-null default for ClassName: $this")
      }
    }
  }
  is Dynamic -> "dynamic"
  is LambdaTypeName -> {
    val parametersList = parameters.joinToString(", ") { "_" }
    val defaultReturnValue = returnType.defaultValue(returnType.isNullable)
    "{ $parametersList -> $defaultReturnValue }"
  }
  is ParameterizedTypeName -> {
    when (rawType) {
      ARRAY -> "emptyArray()"
      BOOLEAN_ARRAY -> "booleanArrayOf()"
      BYTE_ARRAY -> "byteArrayOf()"
      CHAR_ARRAY -> "charArrayOf()"
      SHORT_ARRAY -> "shortArrayOf()"
      INT_ARRAY -> "intArrayOf()"
      LONG_ARRAY -> "longArrayOf()"
      FLOAT_ARRAY -> "floatArrayOf()"
      DOUBLE_ARRAY -> "doubleArrayOf()"
      U_BYTE_ARRAY -> "ubyteArrayOf()"
      U_SHORT_ARRAY -> "ushortArrayOf()"
      U_INT_ARRAY -> "uintArrayOf()"
      U_LONG_ARRAY -> "ulongArrayOf()"
      ITERABLE -> "emptyList()"
      COLLECTION -> "emptyList()"
      LIST -> "emptyList()"
      SET -> "emptySet()"
      MAP -> "emptyMap()"
      MUTABLE_ITERABLE -> "mutableListOf()"
      MUTABLE_COLLECTION -> "mutableListOf()"
      MUTABLE_LIST -> "mutableListOf()"
      MUTABLE_SET -> "mutableSetOf()"
      MUTABLE_MAP -> "mutableMapOf()"
      else -> error("Unable to provide non-null default for ParameterizedTypeName: $this")
    }
  }
  is TypeVariableName -> {
    if (nullable) {
      "null"
    } else {
      error("Unable to provide non-null default for TypeVariableName: $this")
    }
  }
  is WildcardTypeName -> {
    if (nullable) {
      "null"
    } else {
      error("TODO: Default value for WildcardTypeName: $this")
    }
  }
}