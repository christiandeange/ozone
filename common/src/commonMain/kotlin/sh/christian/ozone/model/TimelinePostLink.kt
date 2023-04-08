package sh.christian.ozone.model

import app.bsky.richtext.Facet
import app.bsky.richtext.FacetFeatureUnion
import sh.christian.ozone.model.LinkTarget.ExternalLink
import sh.christian.ozone.model.LinkTarget.UserMention

data class TimelinePostLink(
  val start: Int,
  val end: Int,
  val target: LinkTarget,
)

sealed interface LinkTarget {
  data class UserMention(
    val did: String,
  ) : LinkTarget

  data class ExternalLink(
    val url: String,
  ) : LinkTarget
}

fun Facet.toLink(): TimelinePostLink {
  return TimelinePostLink(
    start = index.byteStart.toInt(),
    end = index.byteEnd.toInt(),
    target = when (val feature = features.first()) {
      is FacetFeatureUnion.Link -> ExternalLink(feature.value.uri)
      is FacetFeatureUnion.Mention -> UserMention(feature.value.did)
    },
  )
}
