package sh.christian.ozone.model

import app.bsky.feed.FeedViewPost
import sh.christian.ozone.util.ReadOnlyList
import sh.christian.ozone.util.mapImmutable

data class Timeline(
  val posts: ReadOnlyList<TimelinePost>,
  val cursor: String?,
) {
  companion object {
    fun from(
      posts: List<FeedViewPost>,
      cursor: String?,
    ): Timeline {
      return Timeline(
        posts = posts.mapImmutable { it.toPost() },
        cursor = cursor,
      )
    }
  }
}
