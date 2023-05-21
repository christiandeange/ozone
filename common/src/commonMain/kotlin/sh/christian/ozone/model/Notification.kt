package sh.christian.ozone.model

import app.bsky.notification.ListNotificationsNotification
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
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
  val uri: String,
  val cid: String,
  val author: Profile,
  val reason: String,
  val reasonSubject: String?,
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
}

fun ListNotificationsNotification.toNotification(
  postsByUri: Map<String, TimelinePost>,
): Notification {
  val notificationPost by lazy {
    val postUri = getPostUri()!!
    postsByUri[postUri]
  }

  return Notification(
    uri = uri,
    cid = cid,
    author = author.toProfile(),
    reason = reason,
    reasonSubject = reasonSubject,
    content = when (reason) {
      "like" -> notificationPost?.let(::Liked)
      "repost" -> notificationPost?.let(::Reposted)
      "follow" -> Followed
      "mention" -> notificationPost?.let(::Mentioned)
      "reply" -> notificationPost?.let(::RepliedTo)
      "quote" -> notificationPost?.let(::Quoted)
      else -> null
    },
    isRead = isRead,
    indexedAt = Moment(indexedAt),
  )
}
