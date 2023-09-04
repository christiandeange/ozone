package sh.christian.ozone.model

import app.bsky.notification.ListNotificationsNotification
import app.bsky.notification.ListNotificationsReason.FOLLOW
import app.bsky.notification.ListNotificationsReason.LIKE
import app.bsky.notification.ListNotificationsReason.MENTION
import app.bsky.notification.ListNotificationsReason.QUOTE
import app.bsky.notification.ListNotificationsReason.REPLY
import app.bsky.notification.ListNotificationsReason.REPOST
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Cid
import sh.christian.ozone.api.runtime.ImmutableListSerializer
import sh.christian.ozone.model.Notification.Content.Followed
import sh.christian.ozone.model.Notification.Content.Liked
import sh.christian.ozone.model.Notification.Content.Mentioned
import sh.christian.ozone.model.Notification.Content.Quoted
import sh.christian.ozone.model.Notification.Content.RepliedTo
import sh.christian.ozone.model.Notification.Content.Reposted
import sh.christian.ozone.notifications.NotificationsRepository.Companion.getPostUri

@Serializable
data class Notifications(
  @Serializable(ImmutableListSerializer::class)
  val list: ImmutableList<Notification>,
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

    object Followed : Content

    data class Mentioned(
      val post: TimelinePost,
    ) : Content

    data class RepliedTo(
      val post: TimelinePost,
    ) : Content

    data class Quoted(
      val post: TimelinePost,
    ) : Content
  }

  enum class Reason {
    LIKE,
    REPOST,
    FOLLOW,
    MENTION,
    REPLY,
    QUOTE,
  }
}

fun ListNotificationsNotification.toNotification(
  postsByUri: Map<AtUri, TimelinePost>,
): Notification {
  val notificationPost by lazy {
    val postUri = getPostUri()!!
    postsByUri[postUri]
  }

  val (notificationReason, content) = when (reason) {
    LIKE -> Notification.Reason.LIKE to notificationPost?.let(::Liked)
    REPOST -> Notification.Reason.REPOST to notificationPost?.let(::Reposted)
    FOLLOW -> Notification.Reason.FOLLOW to Followed
    MENTION -> Notification.Reason.MENTION to notificationPost?.let(::Mentioned)
    REPLY -> Notification.Reason.REPLY to notificationPost?.let(::RepliedTo)
    QUOTE -> Notification.Reason.QUOTE to notificationPost?.let(::Quoted)
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
