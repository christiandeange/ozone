package sh.christian.ozone.model

import app.bsky.actor.ProfileView
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
  val did: String,
  val handle: String,
  val displayName: String?,
  val description: String?,
  val avatar: String?,
  val banner: String?,
  val followersCount: Long,
  val followsCount: Long,
  val postsCount: Long,
  val creator: String,
  val indexedAt: Instant?,
  val mutedByMe: Boolean,
  val followingMe: Boolean,
  val followedByMe: Boolean,
)

fun ProfileView.toProfile(): Profile {
  return Profile(
    did = did,
    handle = handle,
    displayName = displayName,
    description = description,
    avatar = avatar,
    banner = banner,
    followersCount = followersCount,
    followsCount = followsCount,
    postsCount = postsCount,
    creator = creator,
    indexedAt = indexedAt?.let { Instant.parse(it) },
    mutedByMe = viewer?.muted != null,
    followingMe = viewer?.following != null,
    followedByMe = viewer?.followedBy != null,
  )
}
