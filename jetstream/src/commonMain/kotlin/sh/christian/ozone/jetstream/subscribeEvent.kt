package sh.christian.ozone.jetstream

import app.bsky.jetstream.SubscribeAccount
import app.bsky.jetstream.SubscribeCommit
import app.bsky.jetstream.SubscribeIdentity
import app.bsky.jetstream.SubscribeMessage

/**
 * Union field property that represents the expected data contained within a [SubscribeMessage].
 */
val SubscribeMessage.content: SubscribeEventContent
  get() = when {
    commit != null -> SubscribeEventContent.Commit(commit)
    identity != null -> SubscribeEventContent.Identity(identity)
    account != null -> SubscribeEventContent.Account(account)
    else -> error("SubscribeEvent has no content")
  }

/**
 * Represents any of the expected data contained within a [SubscribeMessage].
 */
sealed interface SubscribeEventContent {
  /** Represents a [SubscribeCommit] contained within a [SubscribeMessage]. */
  data class Commit(
    val commit: SubscribeCommit,
  ) : SubscribeEventContent

  /** Represents a [SubscribeIdentity] contained within a [SubscribeMessage]. */
  data class Identity(
    val identity: SubscribeIdentity,
  ) : SubscribeEventContent

  /** Represents a [SubscribeAccount] contained within a [SubscribeMessage]. */
  data class Account(
    val account: SubscribeAccount,
  ) : SubscribeEventContent
}
