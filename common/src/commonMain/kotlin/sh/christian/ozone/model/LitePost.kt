package sh.christian.ozone.model

import app.bsky.feed.Post

data class LitePost(
  val text: String,
  val links: List<TimelinePostLink>,
  val createdAt: Moment,
)

fun Post.toLitePost(): LitePost {
  return LitePost(
    text = text,
    links = facets.map { it.toLink() },
    createdAt = Moment(createdAt),
  )
}
