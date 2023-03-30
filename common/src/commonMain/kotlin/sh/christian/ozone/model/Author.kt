package sh.christian.ozone.model

import app.bsky.actor.RefWithInfo

data class Author(
  val did: String,
  val handle: String,
  val displayName: String?,
  val avatar: String?,
  val mutedByMe: Boolean,
  val followingMe: Boolean,
  val followedByMe: Boolean,
)

fun RefWithInfo.toAuthor(): Author {
  return Author(
    did = did,
    handle = handle,
    displayName = displayName,
    avatar = avatar,
    mutedByMe = viewer?.muted != null,
    followingMe = viewer?.following != null,
    followedByMe = viewer?.followedBy != null,
  )
}
