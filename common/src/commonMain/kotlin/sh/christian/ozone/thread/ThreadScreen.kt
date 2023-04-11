package sh.christian.ozone.thread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import sh.christian.ozone.model.Thread
import sh.christian.ozone.model.ThreadPost
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.timeline.components.ThreadPostItem
import sh.christian.ozone.timeline.components.TimelinePostItem
import sh.christian.ozone.timeline.components.feature.InvisiblePostPost
import sh.christian.ozone.ui.compose.AvatarImage
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.compose.rememberSystemInsets
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen
import sh.christian.ozone.user.UserReference

class ThreadScreen(
  private val now: Instant,
  private val thread: Thread,
  private val onExit: () -> Unit,
  private val onRefresh: () -> Unit,
  private val onOpenThread: (TimelinePost) -> Unit,
  private val onOpenUser: (UserReference) -> Unit,
  private val onOpenImage: (OpenImageAction) -> Unit,
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
      LazyColumn(Modifier.fillMaxSize().padding(contentPadding)) {
        itemsIndexed(thread.parents) { i, parentPost ->
          Box {
            ConversationLinks(
              drawAbove = i != 0,
              drawBelow = true,
            )

            SmallThreadPostItem(
              now = now,
              post = parentPost,
              onOpenThread = onOpenThread,
              onOpenUser = onOpenUser,
              onOpenImage = onOpenImage,
            )
          }
        }

        item {
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
            )
          }
        }

        itemsIndexed(thread.replies) { i, reply ->
          Box {
            ConversationLinks(
              drawAbove = false,
              drawBelow = false,
            )

            SmallThreadPostItem(
              now = now,
              post = reply,
              onOpenThread = onOpenThread,
              onOpenUser = onOpenUser,
              onOpenImage = onOpenImage,
            )
          }
        }
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
  onOpenThread: (TimelinePost) -> Unit,
  onOpenUser: (UserReference) -> Unit,
  onOpenImage: (OpenImageAction) -> Unit,
) {
  when (post) {
    is ThreadPost.ViewablePost -> {
      TimelinePostItem(
        now = now,
        post = post.post,
        onOpenThread = onOpenThread,
        onOpenUser = onOpenUser,
        onOpenImage = onOpenImage
      )
    }
    is ThreadPost.NotFoundPost -> {
      ForbiddenPostItem()
    }
  }
}

@Composable
private fun ForbiddenPostItem() {
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
      InvisiblePostPost(onClick = null)
    }
  }
}
