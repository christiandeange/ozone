package sh.christian.ozone.model

import app.bsky.actor.DefsProfileViewBasic
import app.bsky.actor.DefsProfileViewDetailed
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed interface Profile {
  val did: String
  val handle: String
  val displayName: String?
  val avatar: String?
  val mutedByMe: Boolean
  val followingMe: Boolean
  val followedByMe: Boolean
}

@Serializable
data class LiteProfile(
  override val did: String,
  override val handle: String,
  override val displayName: String?,
  override val avatar: String?,
  override val mutedByMe: Boolean,
  override val followingMe: Boolean,
  override val followedByMe: Boolean,
) : Profile

@Serializable
data class FullProfile(
  override val did: String,
  override val handle: String,
  override val displayName: String?,
  val description: String?,
  override val avatar: String?,
  val banner: String?,
  val followersCount: Long,
  val followsCount: Long,
  val postsCount: Long,
  val indexedAt: Instant?,
  override val mutedByMe: Boolean,
  override val followingMe: Boolean,
  override val followedByMe: Boolean,
) : Profile

fun DefsProfileViewDetailed.toProfile(): FullProfile {
  return FullProfile(
    did = did,
    handle = handle,
    displayName = displayName,
    description = description,
    avatar = avatar,
    banner = banner,
    followersCount = followersCount ?: 0,
    followsCount = followsCount ?: 0,
    postsCount = postsCount ?: 0,
    indexedAt = indexedAt?.let { Instant.parse(it) },
    mutedByMe = viewer?.muted != null,
    followingMe = viewer?.following != null,
    followedByMe = viewer?.followedBy != null,
  )
}

fun DefsProfileViewBasic.toProfile(): Profile {
  return LiteProfile(
    did = did,
    handle = handle,
    displayName = displayName,
    avatar = avatar,
    mutedByMe = viewer?.muted != null,
    followingMe = viewer?.following != null,
    followedByMe = viewer?.followedBy != null,
  )
}
