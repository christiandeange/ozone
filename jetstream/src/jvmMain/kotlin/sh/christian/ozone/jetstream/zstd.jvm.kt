package sh.christian.ozone.jetstream

import com.github.luben.zstd.Zstd
import com.github.luben.zstd.ZstdDictDecompress

private lateinit var dictionary: ZstdDictDecompress

internal actual suspend fun initZstd() {
  if (!::dictionary.isInitialized) {
    val dictionaryFile = JetstreamApi::class.java.getResourceAsStream("/files/zstd_dictionary.bin")!!
    dictionary = ZstdDictDecompress(dictionaryFile.readBytes())
  }
}

internal actual suspend fun decompressZstd(data: ByteArray): ByteArray? {
  return Zstd.decompress(data, dictionary, 10 * 1024 * 1024)
}
