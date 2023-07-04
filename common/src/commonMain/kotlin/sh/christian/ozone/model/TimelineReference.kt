package sh.christian.ozone.model

import com.atproto.repo.StrongRef
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Cid

data class TimelineReference(
  val uri: AtUri,
  val cid: Cid,
)

fun StrongRef.toReference(): TimelineReference {
  return TimelineReference(
    uri = uri,
    cid = cid,
  )
}
