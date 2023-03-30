package sh.christian.ozone.model

import app.bsky.feed.FeedViewPostReasonUnion
import kotlinx.datetime.Instant
import sh.christian.ozone.model.TimelinePostReason.TimelinePostRepost

sealed interface TimelinePostReason {
  data class TimelinePostRepost(
    val originalAuthor: Author,
    val indexedAt: Instant,
  ) : TimelinePostReason
}

fun FeedViewPostReasonUnion.toReason(): TimelinePostReason {
  return when (this) {
    is FeedViewPostReasonUnion.ReasonRepost -> {
      TimelinePostRepost(
        originalAuthor = value.by.toAuthor(),
        indexedAt = Instant.parse(value.indexedAt),
      )
    }
  }
}
