package sh.christian.ozone.oauth

import org.kotlincrypto.random.CryptoRand
import kotlin.random.Random

/**
 * A [Random] implementation that uses a multiplatform cryptographic random number generator.
 */
class CryptographicRandom : Random() {
  private val secureRandom = CryptoRand.Default

  override fun nextBits(bitCount: Int): Int {
    val mask = if (bitCount >= Int.SIZE_BITS) -1 else (1 shl bitCount) - 1

    val randBytes = secureRandom.nextBytes(ByteArray(Int.SIZE_BYTES))
    val ret =
      ((randBytes[0].toInt() and 0xFF) shl 0) or
          ((randBytes[1].toInt() and 0xFF) shl 8) or
          ((randBytes[2].toInt() and 0xFF) shl 16) or
          ((randBytes[3].toInt() and 0xFF) shl 24)

    return ret and mask
  }
}
