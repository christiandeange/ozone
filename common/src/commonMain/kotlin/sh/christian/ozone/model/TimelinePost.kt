package sh.christian.ozone.model

import app.bsky.feed.DefsFeedViewPost
import app.bsky.feed.DefsPostView
import app.bsky.feed.Post
import sh.christian.ozone.util.deserialize

data class TimelinePost(
  val uri: String,
  val cid: String,
  val author: Profile,
  val text: String,
  val textLinks: List<TimelinePostLink>,
  val createdAt: Moment,
  val feature: TimelinePostFeature?,
  val replyCount: Long,
  val repostCount: Long,
  val likeCount: Long,
  val indexedAt: Moment,
  val reposted: Boolean,
  val liked: Boolean,
  val labels: List<Label>,
  val reply: TimelinePostReply?,
  val reason: TimelinePostReason?,
)

fun DefsFeedViewPost.toPost(): TimelinePost {
  return post.toPost(
    reply = reply?.toReply(),
    reason = reason?.toReason(),
  )
}

fun DefsPostView.toPost(): TimelinePost {
  return toPost(
    reply = null,
    reason = null
  )
}

fun DefsPostView.toPost(
  reply: TimelinePostReply?,
  reason: TimelinePostReason?,
): TimelinePost {
  // TODO verify via recordType before blindly deserialized.
  val postRecord = Post.serializer().deserialize(record)

  return TimelinePost(
    uri = uri,
    cid = cid,
    author = author.toProfile(),
    text = postRecord.text,
    textLinks = postRecord.facets.map { it.toLink() },
    createdAt = Moment(postRecord.createdAt),
    feature = embed?.toFeature(),
    replyCount = replyCount ?: 0,
    repostCount = repostCount ?: 0,
    likeCount = likeCount ?: 0,
    indexedAt = Moment(indexedAt),
    reposted = viewer?.repost != null,
    liked = viewer?.like != null,
    labels = labels.map { it.toLabel() },
    reply = reply,
    reason = reason,
  )
}
