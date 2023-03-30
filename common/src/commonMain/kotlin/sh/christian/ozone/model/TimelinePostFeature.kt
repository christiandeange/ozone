package sh.christian.ozone.model

import app.bsky.embed.RecordPresentedRecordUnion.PresentedNotFound
import app.bsky.embed.RecordPresentedRecordUnion.PresentedRecord
import app.bsky.feed.Post
import app.bsky.feed.PostViewEmbedUnion
import app.bsky.feed.PostViewEmbedUnion.ImagesPresented
import sh.christian.ozone.model.EmbedPost.InvisibleEmbedPost
import sh.christian.ozone.model.EmbedPost.VisibleEmbedPost
import sh.christian.ozone.model.TimelinePostFeature.ExternalFeature
import sh.christian.ozone.model.TimelinePostFeature.ImagesFeature
import sh.christian.ozone.model.TimelinePostFeature.PostFeature
import sh.christian.ozone.util.deserialize

sealed interface TimelinePostFeature {
  data class ImagesFeature(
    val images: List<EmbedImage>,
  ) : TimelinePostFeature

  data class ExternalFeature(
    val uri: String,
    val title: String,
    val description: String,
    val thumb: String?,
  ) : TimelinePostFeature

  data class PostFeature(
    val post: EmbedPost,
  ) : TimelinePostFeature
}

data class EmbedImage(
  val thumb: String,
  val fullsize: String,
  val alt: String,
)

sealed interface EmbedPost {
  data class VisibleEmbedPost(
    val uri: String,
    val cid: String,
    val author: Author,
    val litePost: LitePost,
  ) : EmbedPost

  data class InvisibleEmbedPost(
    val uri: String,
  ) : EmbedPost
}

fun PostViewEmbedUnion.toFeature(): TimelinePostFeature {
  return when (this) {
    is ImagesPresented -> {
      ImagesFeature(
        images = value.images.map {
          EmbedImage(
            thumb = it.thumb,
            fullsize = it.fullsize,
            alt = it.alt,
          )
        }
      )
    }
    is PostViewEmbedUnion.ExternalPresented -> {
      ExternalFeature(
        uri = value.external.uri,
        title = value.external.title,
        description = value.external.description,
        thumb = value.external.thumb,
      )
    }
    is PostViewEmbedUnion.RecordPresented -> {
      PostFeature(
        post = when (val record = value.record) {
          is PresentedNotFound -> {
            InvisibleEmbedPost(
              uri = record.value.uri,
            )
          }
          is PresentedRecord -> {
            // TODO verify via recordType before blindly deserialized.
            val litePost = Post.serializer().deserialize(record.value.record).toLitePost()

            VisibleEmbedPost(
              uri = record.value.uri,
              cid = record.value.cid,
              author = record.value.author.toAuthor(),
              litePost = litePost,
            )
          }
        },
      )
    }
  }
}
