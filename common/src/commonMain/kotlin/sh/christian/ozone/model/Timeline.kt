package sh.christian.ozone.model

import app.bsky.feed.DefsFeedViewPost
import kotlinx.collections.immutable.ImmutableList
import sh.christian.ozone.util.mapImmutable

data class Timeline(
  val posts: ImmutableList<TimelinePost>,
  val cursor: String?,
) {
  companion object {
    fun from(
      posts: List<DefsFeedViewPost>,
      cursor: String?,
    ): Timeline {
      return Timeline(
        posts = posts.mapImmutable { it.toPost() },
        cursor = cursor,
      )
    }
  }
}
