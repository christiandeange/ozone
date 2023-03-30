package sh.christian.ozone.model

import app.bsky.feed.FeedViewPost

data class Timeline(
  val posts: List<TimelinePost>,
  val cursor: String?,
) {
  companion object {
    fun from(
      posts: List<FeedViewPost>,
      cursor: String?,
    ): Timeline {
      return Timeline(
        posts = posts.map { it.toPost() },
        cursor = cursor,
      )
    }
  }
}
