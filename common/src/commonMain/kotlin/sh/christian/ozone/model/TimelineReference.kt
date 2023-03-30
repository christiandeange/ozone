package sh.christian.ozone.model

import com.atproto.repo.StrongRef

data class TimelineReference(
  val uri: String,
  val cid: String,
)

fun StrongRef.toReference(): TimelineReference {
  return TimelineReference(
    uri = uri,
    cid = cid,
  )
}
