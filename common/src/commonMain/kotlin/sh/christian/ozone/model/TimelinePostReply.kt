package sh.christian.ozone.model

import app.bsky.feed.FeedViewPostReplyRef

data class TimelinePostReply(
  val root: TimelinePost,
  val parent: TimelinePost,
)

fun FeedViewPostReplyRef.toReply(): TimelinePostReply {
  return TimelinePostReply(
    root = root.toPost(),
    parent = parent.toPost(),
  )
}
