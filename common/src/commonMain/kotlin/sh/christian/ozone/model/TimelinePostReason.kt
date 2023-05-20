package sh.christian.ozone.model

import app.bsky.feed.DefsFeedViewPostReasonUnion
import sh.christian.ozone.model.TimelinePostReason.TimelinePostRepost

sealed interface TimelinePostReason {
  data class TimelinePostRepost(
    val repostAuthor: Profile,
    val indexedAt: Moment,
  ) : TimelinePostReason
}

fun DefsFeedViewPostReasonUnion.toReason(): TimelinePostReason {
  return when (this) {
    is DefsFeedViewPostReasonUnion.ReasonRepost -> {
      TimelinePostRepost(
        repostAuthor = value.by.toProfile(),
        indexedAt = Moment(value.indexedAt),
      )
    }
  }
}
