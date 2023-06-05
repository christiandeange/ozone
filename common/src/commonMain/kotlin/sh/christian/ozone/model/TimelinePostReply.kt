package sh.christian.ozone.model

import app.bsky.feed.DefsReplyRef
import app.bsky.feed.DefsReplyRefParentUnion
import app.bsky.feed.DefsReplyRefRootUnion

data class TimelinePostReply(
  val root: TimelinePost?,
  val parent: TimelinePost?,
)

fun DefsReplyRef.toReply(): TimelinePostReply {
  return TimelinePostReply(
    root = when (val root = root) {
      is DefsReplyRefRootUnion.BlockedPost -> null
      is DefsReplyRefRootUnion.NotFoundPost -> null
      is DefsReplyRefRootUnion.PostView -> root.value.toPost()
    },
    parent = when (val parent = parent) {
      is DefsReplyRefParentUnion.BlockedPost -> null
      is DefsReplyRefParentUnion.NotFoundPost -> null
      is DefsReplyRefParentUnion.PostView -> parent.value.toPost()
    }
  )
}
