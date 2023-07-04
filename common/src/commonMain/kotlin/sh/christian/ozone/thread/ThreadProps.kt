package sh.christian.ozone.thread

import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Cid
import sh.christian.ozone.model.Reference
import sh.christian.ozone.model.TimelinePost

sealed interface ThreadProps {
  val uri: AtUri
  val cid: Cid
  val originalPost: TimelinePost?

  data class FromPost(
    override val originalPost: TimelinePost,
  ) : ThreadProps {
    override val uri: AtUri = originalPost.uri
    override val cid: Cid = originalPost.cid
  }

  data class FromReference(
    val reference: Reference,
  ) : ThreadProps {
    override val originalPost: TimelinePost? = null
    override val uri: AtUri = reference.uri
    override val cid: Cid = reference.cid
  }
}
