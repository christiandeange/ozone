package sh.christian.ozone.model

import app.bsky.feed.DefsThreadViewPost
import app.bsky.feed.DefsThreadViewPostParentUnion
import app.bsky.feed.DefsThreadViewPostReplieUnion
import sh.christian.ozone.model.ThreadPost.BlockedPost
import sh.christian.ozone.model.ThreadPost.NotFoundPost
import sh.christian.ozone.model.ThreadPost.ViewablePost

data class Thread(
  val post: TimelinePost,
  val parents: List<ThreadPost>,
  val replies: List<ThreadPost>,
)

sealed interface ThreadPost {
  data class ViewablePost(
    val post: TimelinePost,
    val replies: List<ThreadPost>,
  ) : ThreadPost

  object NotFoundPost : ThreadPost

  object BlockedPost : ThreadPost
}

fun DefsThreadViewPost.toThread(): Thread {
  return Thread(
    post = post.toPost(),
    parents = generateSequence(parent) { parentPost ->
      when (parentPost) {
        is DefsThreadViewPostParentUnion.BlockedPost -> null
        is DefsThreadViewPostParentUnion.NotFoundPost -> null
        is DefsThreadViewPostParentUnion.ThreadViewPost -> parentPost.value.parent
      }
    }
      .map { it.toThreadPost() }
      .toList()
      .reversed(),
    replies = replies.map { reply -> reply.toThreadPost() },
  )
}

fun DefsThreadViewPostParentUnion.toThreadPost(): ThreadPost = when (this) {
  is DefsThreadViewPostParentUnion.ThreadViewPost -> ViewablePost(
    post = value.post.toPost(),
    replies = value.replies.map { it.toThreadPost() }
  )
  is DefsThreadViewPostParentUnion.NotFoundPost -> NotFoundPost
  is DefsThreadViewPostParentUnion.BlockedPost -> BlockedPost
}

fun DefsThreadViewPostReplieUnion.toThreadPost(): ThreadPost = when (this) {
  is DefsThreadViewPostReplieUnion.ThreadViewPost -> ViewablePost(
    post = value.post.toPost(),
    replies = value.replies.map { it.toThreadPost() },
  )
  is DefsThreadViewPostReplieUnion.NotFoundPost -> NotFoundPost
  is DefsThreadViewPostReplieUnion.BlockedPost -> BlockedPost
}
