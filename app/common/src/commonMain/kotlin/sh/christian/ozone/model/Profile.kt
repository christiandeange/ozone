package sh.christian.ozone.model

import app.bsky.actor.ProfileView
import app.bsky.actor.ProfileViewBasic
import app.bsky.actor.ProfileViewDetailed
import app.bsky.actor.VerificationState
import app.bsky.actor.VerificationStateTrustedVerifierStatus
import app.bsky.actor.VerificationStateVerifiedStatus
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import sh.christian.ozone.api.Did
import sh.christian.ozone.api.Handle
import sh.christian.ozone.model.Profile.Verification
import sh.christian.ozone.util.ReadOnlyList
import sh.christian.ozone.util.mapImmutable

@Serializable
sealed interface Profile {
  val did: Did
  val handle: Handle
  val displayName: String?
  val avatar: String?
  val mutedByMe: Boolean
  val followingMe: Boolean
  val followedByMe: Boolean
  val labels: ReadOnlyList<Label>
  val verification: Verification

  enum class Verification {
    NONE,
    VERIFIED,
    TRUSTED_VERIFIER,
  }
}

@Serializable
data class LiteProfile(
  override val did: Did,
  override val handle: Handle,
  override val displayName: String?,
  override val avatar: String?,
  override val mutedByMe: Boolean,
  override val followingMe: Boolean,
  override val followedByMe: Boolean,
  override val labels: ReadOnlyList<Label>,
  override val verification: Verification,
) : Profile

@Serializable
data class FullProfile(
  override val did: Did,
  override val handle: Handle,
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
  override val labels: ReadOnlyList<Label>,
  override val verification: Verification,
) : Profile

fun ProfileViewDetailed.toProfile(): FullProfile {
  return FullProfile(
    did = did,
    handle = handle,
    displayName = displayName,
    description = description,
    avatar = avatar?.uri,
    banner = banner?.uri,
    followersCount = followersCount ?: 0,
    followsCount = followsCount ?: 0,
    postsCount = postsCount ?: 0,
    indexedAt = indexedAt?.let(::Moment),
    mutedByMe = viewer?.muted == true,
    followingMe = viewer?.followedBy != null,
    followedByMe = viewer?.following != null,
    labels = labels?.mapImmutable { it.toLabel() } ?: persistentListOf(),
    verification = verification?.toVerification() ?: Verification.NONE,
  )
}

fun ProfileViewBasic.toProfile(): Profile {
  return LiteProfile(
    did = did,
    handle = handle,
    displayName = displayName,
    avatar = avatar?.uri,
    mutedByMe = viewer?.muted != null,
    followingMe = viewer?.followedBy != null,
    followedByMe = viewer?.following != null,
    labels = labels?.mapImmutable { it.toLabel() } ?: persistentListOf(),
    verification = verification?.toVerification() ?: Verification.NONE,
  )
}

fun ProfileView.toProfile(): Profile {
  return LiteProfile(
    did = did,
    handle = handle,
    displayName = displayName,
    avatar = avatar?.uri,
    mutedByMe = viewer?.muted == true,
    followingMe = viewer?.followedBy != null,
    followedByMe = viewer?.following != null,
    labels = labels?.mapImmutable { it.toLabel() } ?: persistentListOf(),
    verification = verification?.toVerification() ?: Verification.NONE,
  )
}

fun VerificationState.toVerification(): Verification {
  return when (trustedVerifierStatus) {
    is VerificationStateTrustedVerifierStatus.Valid -> Verification.TRUSTED_VERIFIER
    is VerificationStateTrustedVerifierStatus.Unknown,
    is VerificationStateTrustedVerifierStatus.Invalid,
    is VerificationStateTrustedVerifierStatus.None -> {
      when (verifiedStatus) {
        is VerificationStateVerifiedStatus.Valid -> Verification.VERIFIED
        is VerificationStateVerifiedStatus.Invalid,
        is VerificationStateVerifiedStatus.None,
        is VerificationStateVerifiedStatus.Unknown -> Verification.NONE
      }
    }
  }
}
