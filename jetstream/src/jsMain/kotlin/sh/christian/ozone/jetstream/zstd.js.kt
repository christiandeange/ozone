package sh.christian.ozone.jetstream

import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array

private val decompressor = CompletableDeferred<Decompressor>()

internal actual suspend fun initZstd() {
  if (!decompressor.isCompleted) {
    val dictionaryBytes = window.fetch("/files/zstd_dictionary.bin")
      .then { it.arrayBuffer() }
      .then { Int8Array(it) }
      .await()

    decompressor.complete(Decompressor(dictionaryBytes))
  }
}

internal actual suspend fun decompressZstd(data: ByteArray): ByteArray? {
  val decompressed = decompressor.await().decompress(data)?.decodeToString() ?: return null
  val decoded = decodeURIComponent(escape(decompressed))
  return decoded.encodeToByteArray()
}

@Suppress("UnsafeCastFromDynamic", "UNUSED_ANONYMOUS_PARAMETER", "UNUSED_VARIABLE")
private class Decompressor(private val dictionaryBytes: Int8Array) {
  private val decompressor = CompletableDeferred<Zstd.Simple>()
  private val decompressDictionary = CompletableDeferred<Zstd.Dict.Decompression>()

  init {
    ZstdCodec.run { zstd ->
      val bytes = dictionaryBytes
      decompressor.complete(js("new zstd.Simple()"))
      decompressDictionary.complete(js("new zstd.Dict.Decompression(bytes)"))
    }
  }

  suspend fun decompress(data: ByteArray): ByteArray? {
    return decompressor.await().decompressUsingDict(data, decompressDictionary.await())
  }
}
