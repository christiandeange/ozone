package sh.christian.ozone.thread

import sh.christian.ozone.model.Reference
import sh.christian.ozone.model.TimelinePost

sealed interface ThreadProps {
  val uri: String
  val cid: String
  val originalPost: TimelinePost?

  data class FromPost(
    override val originalPost: TimelinePost,
  ) : ThreadProps {
    override val uri: String = originalPost.uri
    override val cid: String = originalPost.cid
  }

  data class FromReference(
    val reference: Reference,
  ) : ThreadProps {
    override val originalPost: TimelinePost? = null
    override val uri: String = reference.uri
    override val cid: String = reference.cid
  }
}
