package sh.christian.ozone.compose

import sh.christian.ozone.model.Profile
import sh.christian.ozone.model.Reference
import sh.christian.ozone.model.TimelinePost

data class ComposePostProps(
  val replyTo: PostReplyInfo? = null,
)

data class PostReplyInfo(
  val parent: Reference,
  val root: Reference,
  val parentAuthor: Profile,
)

fun TimelinePost.asReplyInfo(): PostReplyInfo {
  return PostReplyInfo(
    parent = Reference(uri, cid),
    root = (reply?.root ?: this).let { Reference(it.uri, it.cid) },
    parentAuthor = author,
  )
}
