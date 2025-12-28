package sh.christian.ozone.model

import androidx.compose.runtime.Immutable
import app.bsky.feed.FeedViewPost
import app.bsky.feed.Post
import app.bsky.feed.PostView
import kotlinx.collections.immutable.persistentListOf
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Cid
import sh.christian.ozone.util.ReadOnlyList
import sh.christian.ozone.util.deserialize
import sh.christian.ozone.util.mapImmutable
import sh.christian.ozone.util.mapNotNullImmutable

@Immutable
data class TimelinePost(
  val uri: AtUri,
  val cid: Cid,
  val author: Profile,
  val text: String,
  val textLinks: ReadOnlyList<TimelinePostLink>,
  val createdAt: Moment,
  val feature: TimelinePostFeature?,
  val replyCount: Long,
  val repostCount: Long,
  val likeCount: Long,
  val indexedAt: Moment,
  val reposted: Boolean,
  val liked: Boolean,
  val labels: ReadOnlyList<Label>,
  val reply: TimelinePostReply?,
  val reason: TimelinePostReason?,
  val tags: List<String>,
)

fun FeedViewPost.toPost(): TimelinePost {
  return post.toPost(
    reply = reply?.toReply(),
    reason = reason?.toReasonOrNull(),
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
    textLinks = postRecord.facets?.mapNotNullImmutable { it.toLinkOrNull() } ?: persistentListOf(),
    createdAt = Moment(postRecord.createdAt),
    feature = embed?.toFeature(),
    replyCount = replyCount ?: 0,
    repostCount = repostCount ?: 0,
    likeCount = likeCount ?: 0,
    indexedAt = Moment(indexedAt),
    reposted = viewer?.repost != null,
    liked = viewer?.like != null,
    labels = labels?.mapImmutable { it.toLabel() } ?: persistentListOf(),
    reply = reply,
    reason = reason,
    tags = postRecord.tags ?: listOf(),
  )
}
