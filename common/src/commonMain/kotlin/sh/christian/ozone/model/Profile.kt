package sh.christian.ozone.model

import app.bsky.actor.DefsProfileView
import app.bsky.actor.DefsProfileViewBasic
import app.bsky.actor.DefsProfileViewDetailed
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
  val labels: List<Label>
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
  override val labels: List<Label>,
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
  val indexedAt: Moment?,
  override val mutedByMe: Boolean,
  override val followingMe: Boolean,
  override val followedByMe: Boolean,
  override val labels: List<Label>,
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
    indexedAt = indexedAt?.let(::Moment),
    mutedByMe = viewer?.muted == true,
    followingMe = viewer?.followedBy != null,
    followedByMe = viewer?.following != null,
    labels = labels.map { it.toLabel() },
  )
}

fun DefsProfileViewBasic.toProfile(): Profile {
  return LiteProfile(
    did = did,
    handle = handle,
    displayName = displayName,
    avatar = avatar,
    mutedByMe = viewer?.muted != null,
    followingMe = viewer?.followedBy != null,
    followedByMe = viewer?.following != null,
    labels = labels.map { it.toLabel() },
  )
}

fun DefsProfileView.toProfile(): Profile {
  return LiteProfile(
    did = did,
    handle = handle,
    displayName = displayName,
    avatar = avatar,
    mutedByMe = viewer?.muted == true,
    followingMe = viewer?.followedBy != null,
    followedByMe = viewer?.following != null,
    labels = labels.map { it.toLabel() },
  )
}
