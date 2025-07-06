package sh.christian.ozone.oauth

import io.ktor.util.Digest

/**
 * Represents an OAuth code challenge method that the OAuth server will use to verify the code challenge.
 */
abstract class OAuthCodeChallengeMethod(open val method: String) {
  abstract suspend fun provideCodeChallenge(codeVerifier: String): String

  /**
   * The "plain" code challenge method, which does not apply any transformation to the code verifier.
   * This is not secure and is generally not recommended for production use.
   */
  data object Plain : OAuthCodeChallengeMethod("plain") {
    override suspend fun provideCodeChallenge(codeVerifier: String): String = codeVerifier
  }

  /**
   * The "S256" code challenge method, which applies SHA-256 hashing to the code verifier.
   * This must be supported by all clients and Authorization Servers; see
   * [RFC 7636](https://datatracker.ietf.org/doc/html/rfc7636) for details.
   *
   * The transform involves a relatively simple SHA-256 hash and base64url string encoding. The code value is a set of
   * 32 to 96 random bytes, encoded in base64url (resulting in 43 or more string-encoded characters).
   */
  data object S256 : OAuthCodeChallengeMethod("S256") {
    override suspend fun provideCodeChallenge(codeVerifier: String): String {
      val sha256 = Digest("SHA-256").also { it += codeVerifier.encodeToByteArray() }.build()
      val base64UrlSafe = sha256.encodeBase64Url()
      return base64UrlSafe
    }
  }
}
