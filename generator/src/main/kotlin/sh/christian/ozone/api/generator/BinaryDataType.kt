package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import java.io.Serializable

sealed interface BinaryDataType : Serializable {
  /** Represents binary data in XPRC calls with a ByteArray. */
  object ByteArray : BinaryDataType {
    private fun readResolve(): Any = ByteArray
  }

  /**
   * Represents binary data in XPRC calls with Ktor's ByteReadChannel.
   */
  object ByteReadChannel : BinaryDataType {
    private fun readResolve(): Any = ByteArray
  }
}

fun BinaryDataType.className() =
  when (this) {
    BinaryDataType.ByteArray -> BYTE_ARRAY
    is BinaryDataType.ByteReadChannel -> BYTE_READ_CHANNEL_CLASS_NAME
  }

private val BYTE_READ_CHANNEL_CLASS_NAME = ClassName(
  packageName = "io.ktor.utils.io",
  simpleNames = listOf("ByteReadChannel")
)
