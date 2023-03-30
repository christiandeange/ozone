package sh.christian.ozone.model

import app.bsky.feed.PostEntity

data class TimelinePostLink(
  val start: Int,
  val end: Int,
  val type: String,
  val value: String,
)

fun PostEntity.toLink(): TimelinePostLink {
  return TimelinePostLink(
    start = index.start.toInt(),
    end = index.end.toInt(),
    type = type,
    value = value,
  )
}
