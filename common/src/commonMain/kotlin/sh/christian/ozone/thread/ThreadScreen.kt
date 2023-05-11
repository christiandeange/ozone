package sh.christian.ozone.thread

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import sh.christian.ozone.compose.PostReplyInfo
import sh.christian.ozone.compose.asReplyInfo
import sh.christian.ozone.model.Profile
import sh.christian.ozone.model.Thread
import sh.christian.ozone.model.ThreadPost
import sh.christian.ozone.model.ThreadPost.ViewablePost
import sh.christian.ozone.timeline.components.ThreadPostItem
import sh.christian.ozone.timeline.components.TimelinePostItem
import sh.christian.ozone.timeline.components.feature.BlockedPostPost
import sh.christian.ozone.timeline.components.feature.InvisiblePostPost
import sh.christian.ozone.timeline.components.hasInteractions
import sh.christian.ozone.ui.compose.AvatarImage
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.compose.rememberSystemInsets
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen
import sh.christian.ozone.user.UserReference
import kotlin.math.min

class ThreadScreen(
  private val now: Instant,
  private val thread: Thread,
  private val onExit: () -> Unit,
  private val onRefresh: () -> Unit,
  private val onOpenPost: (ThreadProps) -> Unit,
  private val onOpenUser: (UserReference) -> Unit,
  private val onOpenImage: (OpenImageAction) -> Unit,
  private val onReplyToPost: (PostReplyInfo) -> Unit,
) : ViewRendering by screen({
  Surface(
    modifier = Modifier
      .fillMaxSize()
      .onBackPressed(onExit),
  ) {
    Scaffold(
      modifier = Modifier.padding(rememberSystemInsets()),
      contentWindowInsets = WindowInsets(0.dp),
      topBar = {
        TopAppBar(
          windowInsets = WindowInsets(0.dp),
          navigationIcon = {
            IconButton(onClick = onExit) {
              Icon(
                painter = rememberVectorPainter(Icons.Default.ArrowBack),
                contentDescription = "Back",
              )
            }
          },
          title = { Text("Thread") },
          actions = {
            IconButton(onClick = onRefresh) {
              Icon(
                painter = rememberVectorPainter(Icons.Default.Refresh),
                contentDescription = "Refresh",
              )
            }
          }
        )
      },
    ) { contentPadding ->
      val state = rememberLazyListState()

      LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        state = state,
      ) {
        @OptIn(ExperimentalFoundationApi::class)
        stickyHeader {
          Divider(thickness = Dp.Hairline)
        }

        itemsIndexed(thread.parents) { i, parentPost ->
          key(parentPost) {
            Box {
              ConversationLinks(
                drawAbove = i != 0,
                drawBelow = true,
              )

              SmallThreadPostItem(
                now = now,
                post = parentPost,
                onOpenPost = onOpenPost,
                onOpenUser = onOpenUser,
                onOpenImage = onOpenImage,
                onReplyToPost = { onReplyToPost(it.post.asReplyInfo()) },
              )
            }
          }
        }

        item {
          key(thread.post) {
            Box {
              ConversationLinks(
                drawAbove = thread.parents.isNotEmpty(),
                drawBelow = false,
              )

              ThreadPostItem(
                now = now,
                post = thread.post,
                onOpenUser = onOpenUser,
                onOpenImage = onOpenImage,
                onOpenPost = onOpenPost,
                onReplyToPost = { onReplyToPost(thread.post.asReplyInfo()) },
              )
            }
          }
        }

        items(thread.replies) { reply ->
          key(reply) {
            val replyReplies = reply.withInterestingReplies(
              ops = thread.parents.viewable().map { it.post.author } + thread.post.author,
            )

            replyReplies.forEachIndexed { i, replyPost ->
              Box {
                ConversationLinks(
                  drawAbove = i != 0,
                  drawBelow = i != replyReplies.lastIndex,
                )

                SmallThreadPostItem(
                  now = now,
                  post = replyPost,
                  onOpenPost = onOpenPost,
                  onOpenUser = onOpenUser,
                  onOpenImage = onOpenImage,
                  onReplyToPost = { onReplyToPost(it.post.asReplyInfo()) },
                )
              }
            }
          }
        }

        item {
          Spacer(Modifier.height(48.dp))
        }
      }

      LaunchedEffect(thread.parents.size) {
        // Default scroll position to the highlighted post.
        state.scrollToItem(thread.parents.size)
      }
    }
  }
})

@Composable
private fun BoxScope.ConversationLinks(
  drawAbove: Boolean,
  drawBelow: Boolean,
) {
  val thickness = remember { 2.dp }
  val sidePadding by remember {
    derivedStateOf { 40.dp - (thickness / 2) }
  }

  Column(
    modifier = Modifier
      .matchParentSize()
      .padding(horizontal = sidePadding),
  ) {
    Box(
      modifier = Modifier
        .height(40.dp)
        .background(if (drawAbove) MaterialTheme.colorScheme.outline else Color.Transparent)
        .width(thickness),
    )
    Box(
      modifier = Modifier
        .weight(1f)
        .background(if (drawBelow) MaterialTheme.colorScheme.outline else Color.Transparent)
        .width(thickness),
    )
  }
}

@Composable
private fun SmallThreadPostItem(
  now: Instant,
  post: ThreadPost,
  onOpenPost: (ThreadProps) -> Unit,
  onOpenUser: (UserReference) -> Unit,
  onOpenImage: (OpenImageAction) -> Unit,
  onReplyToPost: (ViewablePost) -> Unit,
) {
  when (post) {
    is ViewablePost -> {
      TimelinePostItem(
        now = now,
        post = post.post,
        onOpenPost = onOpenPost,
        onOpenUser = onOpenUser,
        onOpenImage = onOpenImage,
        onReplyToPost = { onReplyToPost(post) },
      )
    }
    is ThreadPost.NotFoundPost -> {
      ForbiddenPostItem {
        InvisiblePostPost(onClick = null)
      }
    }
    is ThreadPost.BlockedPost -> {
      ForbiddenPostItem {
        BlockedPostPost(onClick = null)
      }
    }
  }
}

@Composable
private fun ForbiddenPostItem(content: @Composable () -> Unit) {
  Row(
    modifier = Modifier.padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    AvatarImage(
      modifier = Modifier.size(48.dp),
      avatarUrl = null,
      onClick = null,
      contentDescription = null,
      fallbackColor = MaterialTheme.colorScheme.outline,
    )

    Column(Modifier.weight(1f)) {
      content()
    }
  }
}

private fun ThreadPost.withInterestingReplies(ops: Collection<Profile>): List<ThreadPost> {
  val conversation: Set<String?> = buildSet {
    addAll(ops.map { it.did })
    when (this@withInterestingReplies) {
      is ViewablePost -> add(post.author.did)
      is ThreadPost.NotFoundPost,
      is ThreadPost.BlockedPost -> Unit
    }
  }

  return generateSequence(this@withInterestingReplies) { post ->
    when (post) {
      is ViewablePost -> {
        val viewableReplies = post.replies.viewable()
        viewableReplies.firstOrNull { replyPost -> replyPost.post.author.did in conversation }
          ?: viewableReplies.firstOrNull { replyPost -> replyPost.post.hasInteractions() }
      }
      is ThreadPost.NotFoundPost,
      is ThreadPost.BlockedPost -> null
    }
  }.toList().let { replyThread ->
    if (
      replyThread.viewable().all { it.post.author.did in conversation }
    ) {
      replyThread.subList(0, min(replyThread.count(), 3))
    } else {
      replyThread
    }
  }
}

private fun List<ThreadPost>.viewable(): List<ViewablePost> =
  filterIsInstance<ViewablePost>()
