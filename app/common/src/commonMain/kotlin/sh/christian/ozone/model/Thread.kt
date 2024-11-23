package sh.christian.ozone.model

import app.bsky.feed.ThreadViewPost
import app.bsky.feed.ThreadViewPostParentUnion
import app.bsky.feed.ThreadViewPostReplieUnion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import sh.christian.ozone.model.ThreadPost.BlockedPost
import sh.christian.ozone.model.ThreadPost.NotFoundPost
import sh.christian.ozone.model.ThreadPost.UnknownPost
import sh.christian.ozone.model.ThreadPost.ViewablePost
import sh.christian.ozone.util.mapImmutable

data class Thread(
  val post: TimelinePost,
  val parents: ImmutableList<ThreadPost>,
  val replies: ImmutableList<ThreadPost>,
)

sealed interface ThreadPost {
  data class ViewablePost(
    val post: TimelinePost,
    val replies: ImmutableList<ThreadPost>,
  ) : ThreadPost

  object NotFoundPost : ThreadPost

  object BlockedPost : ThreadPost

  object UnknownPost : ThreadPost
}

fun ThreadViewPost.toThread(): Thread {
  return Thread(
    post = post.toPost(),
    parents = generateSequence(parent) { parentPost ->
      when (parentPost) {
        is ThreadViewPostParentUnion.BlockedPost -> null
        is ThreadViewPostParentUnion.NotFoundPost -> null
        is ThreadViewPostParentUnion.ThreadViewPost -> parentPost.value.parent
        is ThreadViewPostParentUnion.Unknown -> null
      }
    }
      .map { it.toThreadPost() }
      .toList()
      .reversed()
      .toImmutableList(),
    replies = replies.mapImmutable { reply -> reply.toThreadPost() },
  )
}

fun ThreadViewPostParentUnion.toThreadPost(): ThreadPost = when (this) {
  is ThreadViewPostParentUnion.ThreadViewPost -> ViewablePost(
    post = value.post.toPost(),
    replies = value.replies.mapImmutable { it.toThreadPost() }
  )
  is ThreadViewPostParentUnion.NotFoundPost -> NotFoundPost
  is ThreadViewPostParentUnion.BlockedPost -> BlockedPost
  is ThreadViewPostParentUnion.Unknown -> UnknownPost
}

fun ThreadViewPostReplieUnion.toThreadPost(): ThreadPost = when (this) {
  is ThreadViewPostReplieUnion.ThreadViewPost -> ViewablePost(
    post = value.post.toPost(),
    replies = value.replies.mapImmutable { it.toThreadPost() },
  )
  is ThreadViewPostReplieUnion.NotFoundPost -> NotFoundPost
  is ThreadViewPostReplieUnion.BlockedPost -> BlockedPost
  is ThreadViewPostReplieUnion.Unknown -> UnknownPost
}
