package sh.christian.ozone.model

import app.bsky.feed.DefsFeedViewPostReasonUnion
import kotlinx.datetime.Instant
import sh.christian.ozone.model.TimelinePostReason.TimelinePostRepost

sealed interface TimelinePostReason {
  data class TimelinePostRepost(
    val originalAuthor: Profile,
    val indexedAt: Instant,
  ) : TimelinePostReason
}

fun DefsFeedViewPostReasonUnion.toReason(): TimelinePostReason {
  return when (this) {
    is DefsFeedViewPostReasonUnion.ReasonRepost -> {
      TimelinePostRepost(
        originalAuthor = value.by.toProfile(),
        indexedAt = Instant.parse(value.indexedAt),
      )
    }
  }
}
