package sh.christian.ozone.model

import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Cid

data class Reference(
  val uri: AtUri,
  val cid: Cid,
)
