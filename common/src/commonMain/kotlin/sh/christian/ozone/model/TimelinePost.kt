package sh.christian.ozone.model

import app.bsky.feed.FeedViewPost
import app.bsky.feed.Post
import app.bsky.feed.PostView
import kotlinx.datetime.Instant
import sh.christian.ozone.util.deserialize

data class TimelinePost(
  val uri: String,
  val cid: String,
  val author: Author,
  val text: String,
  val textLinks: List<TimelinePostLink>,
  val createdAt: Instant,
  val feature: TimelinePostFeature?,
  val replyCount: Long,
  val repostCount: Long,
  val upvoteCount: Long,
  val downvoteCount: Long,
  val indexedAt: Instant,
  val reposted: Boolean,
  val upvoted: Boolean,
  val downvoted: Boolean,
  val reply: TimelinePostReply?,
  val reason: TimelinePostReason?,
)

fun FeedViewPost.toPost(): TimelinePost {
  return post.toPost(
    reply = reply?.toReply(),
    reason = reason?.toReason(),
  )
}

fun PostView.toPost(): TimelinePost {
  return toPost(
    reply = null,
    reason = null
  )
}

fun PostView.toPost(
  reply: TimelinePostReply?,
  reason: TimelinePostReason?,
): TimelinePost {
  // TODO verify via recordType before blindly deserialized.
  val postRecord = Post.serializer().deserialize(record)

  return TimelinePost(
    uri = uri,
    cid = cid,
    author = author.toAuthor(),
    text = postRecord.text,
    textLinks = postRecord.entities.map { it.toLink() },
    createdAt = Instant.parse(postRecord.createdAt),
    feature = embed?.toFeature(),
    replyCount = replyCount,
    repostCount = repostCount,
    upvoteCount = upvoteCount,
    downvoteCount = downvoteCount,
    indexedAt = Instant.parse(indexedAt),
    reposted = viewer.repost != null,
    upvoted = viewer.upvote != null,
    downvoted = viewer.downvote != null,
    reply = reply,
    reason = reason,
  )
}
