@file:JsModule("zstd-codec")
@file:JsNonModule

package sh.christian.ozone.jetstream

internal external object ZstdCodec {
  fun run(callback: (Zstd) -> Unit)
}

internal external class Zstd {
  class Simple {
    fun compressUsingDict(data: ByteArray, dict: Dict.Compression): ByteArray
    fun decompressUsingDict(data: ByteArray, dict: Dict.Decompression): ByteArray?
  }

  class Dict {
    class Compression(dictBytes: ByteArray, compressionLevel: Int)
    class Decompression(dictBytes: ByteArray)
  }
}
