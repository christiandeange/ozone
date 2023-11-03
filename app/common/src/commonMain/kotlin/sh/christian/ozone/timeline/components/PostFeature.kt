package sh.christian.ozone.timeline.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import sh.christian.ozone.model.EmbedPost
import sh.christian.ozone.model.Moment
import sh.christian.ozone.model.TimelinePostFeature
import sh.christian.ozone.model.TimelinePostFeature.ExternalFeature
import sh.christian.ozone.model.TimelinePostFeature.ImagesFeature
import sh.christian.ozone.model.TimelinePostFeature.MediaPostFeature
import sh.christian.ozone.model.TimelinePostFeature.PostFeature
import sh.christian.ozone.thread.ThreadProps
import sh.christian.ozone.timeline.components.feature.BlockedPostPost
import sh.christian.ozone.timeline.components.feature.InvisiblePostPost
import sh.christian.ozone.timeline.components.feature.PostExternal
import sh.christian.ozone.timeline.components.feature.PostImages
import sh.christian.ozone.timeline.components.feature.VisiblePostPost
import sh.christian.ozone.ui.compose.OpenImageAction

@Composable
internal fun PostFeature(
  now: Moment,
  feature: TimelinePostFeature?,
  onOpenImage: (OpenImageAction) -> Unit,
  onOpenPost: (ThreadProps) -> Unit,
) {
  val uriHandler = LocalUriHandler.current
  when (feature) {
    is ImagesFeature -> PostImages(feature, onOpenImage)
    is ExternalFeature -> PostExternal(feature, onClick = {
      uriHandler.openUri(feature.uri.uri)
    })
    is PostFeature -> when (val embedPost = feature.post) {
      is EmbedPost.VisibleEmbedPost -> {
        VisiblePostPost(now, embedPost.litePost, embedPost.author, onClick = {
          onOpenPost(ThreadProps.FromReference(embedPost.reference))
        })
      }
      is EmbedPost.InvisibleEmbedPost -> InvisiblePostPost(onClick = {})
      is EmbedPost.BlockedEmbedPost -> BlockedPostPost(onClick = {})
    }
    is MediaPostFeature -> {
      when (val embedMedia = feature.media) {
        is ImagesFeature -> PostImages(embedMedia, onOpenImage)
        is ExternalFeature -> PostExternal(embedMedia, onClick = {
          uriHandler.openUri(embedMedia.uri.uri)
        })
      }
      when (val embedPost = feature.post) {
        is EmbedPost.VisibleEmbedPost -> {
          VisiblePostPost(now, embedPost.litePost, embedPost.author, onClick = {
            onOpenPost(ThreadProps.FromReference(embedPost.reference))
          })
        }
        is EmbedPost.InvisibleEmbedPost -> InvisiblePostPost(onClick = {})
        is EmbedPost.BlockedEmbedPost -> BlockedPostPost(onClick = {})
      }
    }
    null -> Unit
  }
}
