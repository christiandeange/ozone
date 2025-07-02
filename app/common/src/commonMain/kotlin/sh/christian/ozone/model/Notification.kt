package sh.christian.ozone.model

import app.bsky.notification.ListNotificationsNotification
import app.bsky.notification.ListNotificationsReason.Follow
import app.bsky.notification.ListNotificationsReason.Like
import app.bsky.notification.ListNotificationsReason.LikeViaRepost
import app.bsky.notification.ListNotificationsReason.Mention
import app.bsky.notification.ListNotificationsReason.Quote
import app.bsky.notification.ListNotificationsReason.Reply
import app.bsky.notification.ListNotificationsReason.Repost
import app.bsky.notification.ListNotificationsReason.RepostViaRepost
import app.bsky.notification.ListNotificationsReason.StarterpackJoined
import app.bsky.notification.ListNotificationsReason.SubscribedPost
import app.bsky.notification.ListNotificationsReason.Unknown
import app.bsky.notification.ListNotificationsReason.Unverified
import app.bsky.notification.ListNotificationsReason.Verified
import kotlinx.serialization.Serializable
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Cid
import sh.christian.ozone.model.Notification.Content.Followed
import sh.christian.ozone.model.Notification.Content.JoinedStarterPack
import sh.christian.ozone.model.Notification.Content.Liked
import sh.christian.ozone.model.Notification.Content.LikedViaRepost
import sh.christian.ozone.model.Notification.Content.Mentioned
import sh.christian.ozone.model.Notification.Content.Quoted
import sh.christian.ozone.model.Notification.Content.RepliedTo
import sh.christian.ozone.model.Notification.Content.Reposted
import sh.christian.ozone.model.Notification.Content.RepostedViaRepost
import sh.christian.ozone.model.Notification.Content.UserUnverified
import sh.christian.ozone.model.Notification.Content.UserVerified
import sh.christian.ozone.notifications.NotificationsRepository.Companion.getPostUri
import sh.christian.ozone.util.ReadOnlyList

@Serializable
data class Notifications(
  val list: ReadOnlyList<Notification>,
  val cursor: String?,
)

@Serializable
data class Notification(
  val uri: AtUri,
  val cid: Cid,
  val author: Profile,
  val reason: Reason,
  val reasonSubject: AtUri?,
  val content: Content?,
  val isRead: Boolean,
  val indexedAt: Moment,
) {
  sealed interface Content {
    data class Liked(
      val post: TimelinePost,
    ) : Content

    data class Reposted(
      val post: TimelinePost,
    ) : Content

    data object Followed : Content

    data class Mentioned(
      val post: TimelinePost,
    ) : Content

    data class RepliedTo(
      val post: TimelinePost,
    ) : Content

    data class Quoted(
      val post: TimelinePost,
    ) : Content

    data object JoinedStarterPack : Content

    data object UserVerified : Content

    data object UserUnverified : Content

    data class LikedViaRepost(
      val post: TimelinePost,
    ) : Content

    data class RepostedViaRepost(
      val post: TimelinePost,
    ) : Content
  }

  enum class Reason {
    UNKNOWN,
    LIKE,
    REPOST,
    FOLLOW,
    MENTION,
    REPLY,
    QUOTE,
    JOINED_STARTERPACK,
    VERIFIED,
    UNVERIFIED,
    LIKE_VIA_REPOST,
    REPOST_VIA_REPOST,
  }
}

fun ListNotificationsNotification.toNotification(
  postsByUri: Map<AtUri, TimelinePost>,
): Notification? {
  val notificationPost by lazy {
    val postUri = getPostUri()!!
    postsByUri[postUri]
  }

  val (notificationReason, content) = when (reason) {
    is Unknown -> return null
    is Like -> Notification.Reason.LIKE to notificationPost?.let(::Liked)
    is Repost -> Notification.Reason.REPOST to notificationPost?.let(::Reposted)
    is Follow -> Notification.Reason.FOLLOW to Followed
    is Mention -> Notification.Reason.MENTION to notificationPost?.let(::Mentioned)
    is Reply -> Notification.Reason.REPLY to notificationPost?.let(::RepliedTo)
    is Quote -> Notification.Reason.QUOTE to notificationPost?.let(::Quoted)
    is StarterpackJoined -> Notification.Reason.JOINED_STARTERPACK to JoinedStarterPack
    is Verified -> Notification.Reason.VERIFIED to UserVerified
    is Unverified -> Notification.Reason.UNVERIFIED to UserUnverified
    is LikeViaRepost -> Notification.Reason.LIKE_VIA_REPOST to notificationPost?.let(::LikedViaRepost)
    is RepostViaRepost -> Notification.Reason.REPOST_VIA_REPOST to notificationPost?.let(::RepostedViaRepost)
    is SubscribedPost -> return null
  }

  return Notification(
    uri = uri,
    cid = cid,
    author = author.toProfile(),
    reason = notificationReason,
    reasonSubject = reasonSubject,
    content = content,
    isRead = isRead,
    indexedAt = Moment(indexedAt),
  )
}
