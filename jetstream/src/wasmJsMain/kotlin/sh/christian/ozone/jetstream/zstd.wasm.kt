@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package sh.christian.ozone.jetstream

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.await
import kotlin.JsFun
import kotlin.js.JsAny
import kotlin.js.JsName
import kotlin.js.Promise

@JsName("Uint8Array")
internal external class Uint8Array(length: Int) : JsAny {
  val length: Int
  operator fun get(index: Int): Int
  operator fun set(index: Int, value: Int)
}

@JsFun("(url) => fetch(url).then((response) => response.arrayBuffer()).then((buffer) => new Uint8Array(buffer))")
private external fun fetchBytes(url: String): Promise<Uint8Array>

@JsFun("(zstd) => new zstd.Simple()")
private external fun createDecompressor(zstd: Zstd): ZstdSimple

@JsFun("(zstd, dictionaryBytes) => new zstd.Dict.Decompression(dictionaryBytes)")
private external fun createDictionary(
  zstd: Zstd,
  dictionaryBytes: Uint8Array,
): ZstdDecompressionDictionary

private val decompressor = CompletableDeferred<Decompressor>()

internal actual suspend fun initZstd() {
  if (!decompressor.isCompleted) {
    decompressor.complete(Decompressor(fetchBytes("/files/zstd_dictionary.bin").await()))
  }
}

internal actual suspend fun decompressZstd(data: ByteArray): ByteArray? {
  return decompressor.await().decompress(data)
}

private class Decompressor(dictionaryBytes: Uint8Array) {
  private val decompressor = CompletableDeferred<ZstdSimple>()
  private val dictionary = CompletableDeferred<ZstdDecompressionDictionary>()

  init {
    ZstdCodec.run { zstd ->
      decompressor.complete(createDecompressor(zstd))
      dictionary.complete(createDictionary(zstd, dictionaryBytes))
    }
  }

  suspend fun decompress(data: ByteArray): ByteArray? {
    return decompressor.await()
      .decompressUsingDict(data.toUint8Array(), dictionary.await())
      ?.toByteArray()
  }
}

private fun ByteArray.toUint8Array(): Uint8Array = Uint8Array(size).also { destination ->
  forEachIndexed { index, byte -> destination[index] = byte.toInt() and 0xff }
}

private fun Uint8Array.toByteArray(): ByteArray = ByteArray(length) { index -> this[index].toByte() }
