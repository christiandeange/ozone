package sh.christian.ozone.oauth

/**
 * Interface for selecting an OAuth code challenge method based on the supported methods provided by the OAuth
 * authorization server.
 */
fun interface OAuthCodeChallengeMethodSelector {
  fun selectCodeChallengeMethod(supportedChallengeMethods: List<String>): OAuthCodeChallengeMethod
}
