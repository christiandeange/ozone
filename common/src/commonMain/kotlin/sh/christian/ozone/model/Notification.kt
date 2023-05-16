package sh.christian.ozone.model

import app.bsky.graph.Follow
import app.bsky.notification.ListNotificationsNotification
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import sh.christian.ozone.model.Notification.Content.Followed
import sh.christian.ozone.model.Notification.Content.Liked
import sh.christian.ozone.model.Notification.Content.Mentioned
import sh.christian.ozone.model.Notification.Content.Quoted
import sh.christian.ozone.model.Notification.Content.RepliedTo
import sh.christian.ozone.model.Notification.Content.Reposted
import sh.christian.ozone.notifications.NotificationsRepository.Companion.getPostUri
import sh.christian.ozone.util.deserialize

@Serializable
data class Notifications(
  val list: List<Notification>,
  val cursor: String?,
)

@Serializable
data class Notification(
  val uri: String,
  val cid: String,
  val author: Profile,
  val reason: String,
  val reasonSubject: String?,
  val content: Content?,
  val isRead: Boolean,
  val indexedAt: Instant,
) {
  sealed interface Content {
    data class Liked(
      val post: TimelinePost,
    ) : Content

    data class Reposted(
      val post: TimelinePost,
    ) : Content

    data class Followed(
      val follow: Follow,
    ) : Content

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
}

fun ListNotificationsNotification.toNotification(
  postsByUri: Map<String, TimelinePost>,
): Notification {
  val notificationPost by lazy { postsByUri[getPostUri()!!]!! }

  return Notification(
    uri = uri,
    cid = cid,
    author = author.toProfile(),
    reason = reason,
    reasonSubject = reasonSubject,
    content = when (reason) {
      "like" -> Liked(notificationPost)
      "repost" -> Reposted(notificationPost)
      "follow" -> Followed(Follow.serializer().deserialize(record))
      "mention" -> Mentioned(notificationPost)
      "reply" -> RepliedTo(notificationPost)
      "quote" -> Quoted(notificationPost)
      else -> null
    },
    isRead = isRead,
    indexedAt = indexedAt,
  )
}
