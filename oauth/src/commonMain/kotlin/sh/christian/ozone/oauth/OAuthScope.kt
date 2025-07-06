package sh.christian.ozone.oauth

import kotlinx.serialization.Serializable
import sh.christian.ozone.oauth.OAuthScope.Companion.Generic

/**
 * OAuth scopes allow more granular control over the resources and actions a client is granted access to.
 */
@Serializable
data class OAuthScope(val value: String) {
  companion object {
    /**
     * This scope is required for all atproto OAuth sessions. The semantics are somewhat similar to the `openid` scope:
     * inclusion of it confirms that the client is using the `atproto` profile of OAuth and will comply with all the
     * requirements laid out in this specification. No access to any atproto-specific PDS resources will be granted
     * without this scope included.
     */
    val AtProto = OAuthScope("atproto")

    /**
     * Broad PDS account permissions, equivalent to the previous "App Password" authorization level:
     *
     * - Write (create/update/delete) any repository record type
     * - Upload blobs (media files)
     * - Read and write any personal preferences
     * - API endpoints and service proxying for most Lexicon endpoints, to any service provider (identified by DID)
     * - Ability to generate service auth tokens for the specific API endpoints the client has access to
     */
    val Generic = OAuthScope("transition:generic")

    /**
     * Equivalent to adding the "DM Access" toggle for "App Passwords"
     *
     * - API endpoints and service proxying for the `chat.bsky` Lexicons specifically
     * - Ability to generate service auth tokens for the `chat.bsky` Lexicons
     * - This scope depends on and does not function without [Generic]
     */
    val BlueskyChat = OAuthScope("transition:chat.bsky")

    /**
     * Access to the account email address
     *
     * - Email address (and confirmation status) gets included in response to `com.atproto.server.getSession` endpoint
     */
    val Email = OAuthScope("transition:email")
  }
}
