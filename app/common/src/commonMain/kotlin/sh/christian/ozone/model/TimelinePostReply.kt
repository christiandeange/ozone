package sh.christian.ozone.model

import app.bsky.feed.ReplyRef
import app.bsky.feed.ReplyRefParentUnion
import app.bsky.feed.ReplyRefRootUnion

data class TimelinePostReply(
  val root: TimelinePost?,
  val parent: TimelinePost?,
)

fun ReplyRef.toReply(): TimelinePostReply {
  return TimelinePostReply(
    root = when (val root = root) {
      is ReplyRefRootUnion.BlockedPost -> null
      is ReplyRefRootUnion.NotFoundPost -> null
      is ReplyRefRootUnion.Unknown -> null
      is ReplyRefRootUnion.PostView -> root.value.toPost()
    },
    parent = when (val parent = parent) {
      is ReplyRefParentUnion.BlockedPost -> null
      is ReplyRefParentUnion.NotFoundPost -> null
      is ReplyRefParentUnion.Unknown -> null
      is ReplyRefParentUnion.PostView -> parent.value.toPost()
    }
  )
}
