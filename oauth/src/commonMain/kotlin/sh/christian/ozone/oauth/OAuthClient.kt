package sh.christian.ozone.oauth

/**
 * Represents an OAuth client with its client ID and redirect URI.
 *
 * @param clientId The unique identifier for the OAuth client.
 * @param redirectUri The URI to which the authorization server will redirect the user after authorization.
 *                    This URI must match one of the redirect URIs registered for the client.
 */
data class OAuthClient(
  val clientId: String,
  val redirectUri: String,
)
