package sh.christian.ozone.model

import app.bsky.feed.Post
import kotlinx.datetime.Instant

data class LitePost(
  val text: String,
  val links: List<TimelinePostLink>,
  val createdAt: Instant,
)

fun Post.toLitePost(): LitePost {
  return LitePost(
    text = text,
    links = facets.map { it.toLink() },
    createdAt = createdAt,
  )
}
