package sh.christian.ozone.model

import app.bsky.embed.ExternalView
import app.bsky.embed.ImagesView
import app.bsky.embed.RecordViewRecordUnion
import app.bsky.embed.RecordWithMediaViewMediaUnion
import app.bsky.feed.DefsPostViewEmbedUnion
import app.bsky.feed.Post
import kotlinx.collections.immutable.ImmutableList
import sh.christian.ozone.model.EmbedPost.BlockedEmbedPost
import sh.christian.ozone.model.EmbedPost.InvisibleEmbedPost
import sh.christian.ozone.model.EmbedPost.VisibleEmbedPost
import sh.christian.ozone.model.TimelinePostFeature.ExternalFeature
import sh.christian.ozone.model.TimelinePostFeature.ImagesFeature
import sh.christian.ozone.model.TimelinePostFeature.MediaPostFeature
import sh.christian.ozone.model.TimelinePostFeature.PostFeature
import sh.christian.ozone.util.deserialize
import sh.christian.ozone.util.mapImmutable

sealed interface TimelinePostFeature {
  data class ImagesFeature(
    val images: ImmutableList<EmbedImage>,
  ) : TimelinePostFeature, TimelinePostMedia

  data class ExternalFeature(
    val uri: String,
    val title: String,
    val description: String,
    val thumb: String?,
  ) : TimelinePostFeature, TimelinePostMedia

  data class PostFeature(
    val post: EmbedPost,
  ) : TimelinePostFeature

  data class MediaPostFeature(
    val post: EmbedPost,
    val media: TimelinePostMedia,
  ) : TimelinePostFeature
}

sealed interface TimelinePostMedia

data class EmbedImage(
  val thumb: String,
  val fullsize: String,
  val alt: String,
)

sealed interface EmbedPost {
  data class VisibleEmbedPost(
    val uri: String,
    val cid: String,
    val author: Profile,
    val litePost: LitePost,
  ) : EmbedPost {
    val reference: Reference = Reference(uri, cid)
  }

  data class InvisibleEmbedPost(
    val uri: String,
  ) : EmbedPost

  data class BlockedEmbedPost(
    val uri: String,
  ) : EmbedPost
}

fun DefsPostViewEmbedUnion.toFeature(): TimelinePostFeature {
  return when (this) {
    is DefsPostViewEmbedUnion.ImagesView -> {
      value.toImagesFeature()
    }
    is DefsPostViewEmbedUnion.ExternalView -> {
      value.toExternalFeature()
    }
    is DefsPostViewEmbedUnion.RecordView -> {
      PostFeature(
        post = value.record.toEmbedPost(),
      )
    }
    is DefsPostViewEmbedUnion.RecordWithMediaView -> {
      MediaPostFeature(
        post = value.record.record.toEmbedPost(),
        media = when (val media = value.media) {
          is RecordWithMediaViewMediaUnion.ExternalView -> media.value.toExternalFeature()
          is RecordWithMediaViewMediaUnion.ImagesView -> media.value.toImagesFeature()
        },
      )
    }
  }
}

private fun ImagesView.toImagesFeature(): ImagesFeature {
  return ImagesFeature(
    images = images.mapImmutable {
      EmbedImage(
        thumb = it.thumb,
        fullsize = it.fullsize,
        alt = it.alt,
      )
    }
  )
}

private fun ExternalView.toExternalFeature(): ExternalFeature {
  return ExternalFeature(
    uri = external.uri,
    title = external.title,
    description = external.description,
    thumb = external.thumb,
  )
}

private fun RecordViewRecordUnion.toEmbedPost(): EmbedPost {
  return when (this) {
    is RecordViewRecordUnion.ViewBlocked -> {
      BlockedEmbedPost(
        uri = value.uri,
      )
    }
    is RecordViewRecordUnion.ViewNotFound -> {
      InvisibleEmbedPost(
        uri = value.uri,
      )
    }
    is RecordViewRecordUnion.ViewRecord -> {
      // TODO verify via recordType before blindly deserialized.
      val litePost = Post.serializer().deserialize(value.value).toLitePost()

      VisibleEmbedPost(
        uri = value.uri,
        cid = value.cid,
        author = value.author.toProfile(),
        litePost = litePost,
      )
    }
  }
}
