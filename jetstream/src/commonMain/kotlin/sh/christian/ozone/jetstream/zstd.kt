package sh.christian.ozone.jetstream

internal expect suspend fun initZstd()

/**
 * Decompress a Zstandard-compressed byte array and return the decompressed data.
 */
internal expect suspend fun decompressZstd(data: ByteArray): ByteArray?
