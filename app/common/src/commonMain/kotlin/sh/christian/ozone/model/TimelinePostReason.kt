package sh.christian.ozone.model

import app.bsky.feed.FeedViewPostReasonUnion
import sh.christian.ozone.model.TimelinePostReason.TimelinePostRepost

sealed interface TimelinePostReason {
  data class TimelinePostRepost(
    val repostAuthor: Profile,
    val indexedAt: Moment,
  ) : TimelinePostReason
}

fun FeedViewPostReasonUnion.toReason(): TimelinePostReason {
  return when (this) {
    is FeedViewPostReasonUnion.ReasonRepost -> {
      TimelinePostRepost(
        repostAuthor = value.by.toProfile(),
        indexedAt = Moment(value.indexedAt),
      )
    }
  }
}
