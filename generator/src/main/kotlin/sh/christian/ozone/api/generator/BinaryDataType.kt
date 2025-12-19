package sh.christian.ozone.api.generator

import java.io.Serializable

sealed interface BinaryDataType : Serializable {
  /** Represents binary data in XPRC calls with a ByteArray. */
  object ByteArray : BinaryDataType {
    private fun readResolve(): Any = ByteArray
  }

  /**
   * Represents binary data in XPRC calls with the class  specified with the arguments provided.
   * For example, calling this with package name
   * `"java.util"` and simple names `"Map"`, `"Entry"` yields `Map.Entry`.
   */
  data class Custom(
    val packageName: String,
    val simpleNames: List<String>,
  ) : BinaryDataType {
    private fun readResolve(): Any = this
  }
}
