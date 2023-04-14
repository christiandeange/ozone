package sh.christian.ozone.model

import app.bsky.feed.Like
import app.bsky.feed.Post
import app.bsky.feed.Repost
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
      val like: Like,
    ) : Content

    data class Reposted(
      val repost: Repost,
    ) : Content

    data class Followed(
      val follow: Follow,
    ) : Content

    data class Mentioned(
      val mention: LitePost,
    ) : Content

    data class RepliedTo(
      val reply: LitePost,
    ) : Content

    data class Quoted(
      val quote: LitePost,
    ) : Content
  }
}

fun ListNotificationsNotification.toNotification(): Notification {
  return Notification(
    uri = uri,
    cid = cid,
    author = author.toProfile(),
    reason = reason,
    reasonSubject = reasonSubject,
    content = null,
//    content = when (reason) {
//      "like" -> Liked(Like.serializer().deserialize(record))
//      "repost" -> Reposted(Repost.serializer().deserialize(record))
//      "follow" -> Followed(Follow.serializer().deserialize(record))
//      "mention" -> Mentioned(Post.serializer().deserialize(record).toLitePost())
//      "reply" -> RepliedTo(Post.serializer().deserialize(record).toLitePost())
//      "quote" -> Quoted(Post.serializer().deserialize(record).toLitePost())
//      else -> null
//    },
    isRead = isRead,
    indexedAt = indexedAt,
  )
}
