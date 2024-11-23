package sh.christian.ozone.model

import app.bsky.feed.Post
import kotlinx.collections.immutable.ImmutableList
import sh.christian.ozone.util.mapNotNullImmutable

data class LitePost(
  val text: String,
  val links: ImmutableList<TimelinePostLink>,
  val createdAt: Moment,
)

fun Post.toLitePost(): LitePost {
  return LitePost(
    text = text,
    links = facets.mapNotNullImmutable { it.toLinkOrNull() },
    createdAt = Moment(createdAt),
  )
}
