package sh.christian.ozone.model

import androidx.compose.runtime.Immutable
import app.bsky.feed.FeedViewPost
import app.bsky.feed.Post
import app.bsky.feed.PostView
import kotlinx.collections.immutable.ImmutableList
import sh.christian.ozone.util.deserialize
import sh.christian.ozone.util.mapImmutable

@Immutable
data class TimelinePost(
  val uri: String,
  val cid: String,
  val author: Profile,
  val text: String,
  val textLinks: ImmutableList<TimelinePostLink>,
  val createdAt: Moment,
  val feature: TimelinePostFeature?,
  val replyCount: Long,
  val repostCount: Long,
  val likeCount: Long,
  val indexedAt: Moment,
  val reposted: Boolean,
  val liked: Boolean,
  val labels: ImmutableList<Label>,
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
    author = author.toProfile(),
    text = postRecord.text,
    textLinks = postRecord.facets.mapImmutable { it.toLink() },
    createdAt = Moment(postRecord.createdAt),
    feature = embed?.toFeature(),
    replyCount = replyCount ?: 0,
    repostCount = repostCount ?: 0,
    likeCount = likeCount ?: 0,
    indexedAt = Moment(indexedAt),
    reposted = viewer?.repost != null,
    liked = viewer?.like != null,
    labels = labels.mapImmutable { it.toLabel() },
    reply = reply,
    reason = reason,
  )
}
