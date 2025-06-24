package sh.christian.ozone.oauth.dpop

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.EC.Curve.Companion.P256
import dev.whyoleg.cryptography.algorithms.ECDSA

/**
 * Represents a DPoP (Demonstrating Proof of Possession) key pair, which consists of a public and private key pair.
 *
 * This key pair is used to sign DPoP headers for OAuth requests, ensuring that the request is made by the holder of the
 * private key (ie: this application instance). DPoP keys are also used to authorize requests made to an atproto server
 * like Bluesky.
 *
 * Keys can be imported or exported in various formats depending on the target platform currently being used:
 * - **JVM**: Does not support JWK for importing or exporting keys.
 * - **Android**: Does not support JWK for importing or exporting keys.
 * - **iOS**: Does not support JWK for importing or exporting keys, and does not support RAW for importing keys.
 */
class DpopKeyPair
internal constructor(
  internal val keyPair: ECDSA.KeyPair,
) {
  /** Export the public key of the DPoP keypair in the specified format. */
  suspend fun publicKey(format: PublicKeyFormat): ByteArray =
    keyPair.publicKey.encodeToByteArray(format.format)

  /** Export the private key of the DPoP keypair in the specified format. */
  suspend fun privateKey(format: PrivateKeyFormat): ByteArray =
    keyPair.privateKey.encodeToByteArray(format.format)

  enum class PublicKeyFormat {
    JWK,
    RAW,
    PEM,
    DER,
    ;

    internal val format: EC.PublicKey.Format
      get() = when (this) {
        JWK -> EC.PublicKey.Format.JWK
        RAW -> EC.PublicKey.Format.RAW
        PEM -> EC.PublicKey.Format.PEM
        DER -> EC.PublicKey.Format.DER
      }
  }

  enum class PrivateKeyFormat {
    JWK,
    RAW,
    DER,
    DER_SEC1,
    PEM,
    PEM_SEC1,
    ;

    internal val format: EC.PrivateKey.Format
      get() = when (this) {
        JWK -> EC.PrivateKey.Format.JWK
        RAW -> EC.PrivateKey.Format.RAW
        DER -> EC.PrivateKey.Format.DER
        DER_SEC1 -> EC.PrivateKey.Format.DER.SEC1
        PEM -> EC.PrivateKey.Format.PEM
        PEM_SEC1 -> EC.PrivateKey.Format.PEM.SEC1
      }
  }

  companion object {
    /**
     * Creates a DPoP key pair from the provided public and private keys in the specified formats.
     */
    suspend fun fromKeyPair(
      publicKey: ByteArray,
      publicKeyFormat: PublicKeyFormat,
      privateKey: ByteArray,
      privateKeyFormat: PrivateKeyFormat,
    ): DpopKeyPair {
      val ecdsa: ECDSA = CryptographyProvider.Default.get(ECDSA)
      val decodedPublicKey = ecdsa.publicKeyDecoder(P256).decodeFromByteArray(publicKeyFormat.format, publicKey)
      val decodedPrivateKey = ecdsa.privateKeyDecoder(P256).decodeFromByteArray(privateKeyFormat.format, privateKey)

      val dpopKeyPair = SimpleKeyPair(
        publicKey = decodedPublicKey,
        privateKey = decodedPrivateKey,
      )

      return DpopKeyPair(dpopKeyPair)
    }
  }

  @OptIn(CryptographyProviderApi::class)
  private class SimpleKeyPair(
    override val publicKey: ECDSA.PublicKey,
    override val privateKey: ECDSA.PrivateKey,
  ) : ECDSA.KeyPair
}
