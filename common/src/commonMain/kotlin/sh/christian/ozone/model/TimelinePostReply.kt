package sh.christian.ozone.model

import app.bsky.feed.DefsReplyRef

data class TimelinePostReply(
  val root: TimelinePost,
  val parent: TimelinePost,
)

fun DefsReplyRef.toReply(): TimelinePostReply {
  return TimelinePostReply(
    root = root.toPost(),
    parent = parent.toPost(),
  )
}
