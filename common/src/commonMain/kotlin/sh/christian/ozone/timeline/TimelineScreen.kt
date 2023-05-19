package sh.christian.ozone.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import sh.christian.ozone.compose.PostReplyInfo
import sh.christian.ozone.compose.asReplyInfo
import sh.christian.ozone.model.Profile
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.thread.ThreadProps
import sh.christian.ozone.timeline.components.TimelinePostItem
import sh.christian.ozone.ui.compose.AvatarImage
import sh.christian.ozone.ui.compose.InfiniteListHandler
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.ui.compose.heroFont
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.icons.ChatBubbleOutline
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen
import sh.christian.ozone.user.UserReference
import sh.christian.ozone.util.color

class TimelineScreen(
  private val now: Instant,
  private val profile: Profile?,
  private val timeline: List<TimelinePost>,
  private val showRefreshPrompt: Boolean,
  private val showComposePostButton: Boolean,
  private val onRefresh: () -> Unit,
  private val onLoadMore: () -> Unit,
  private val onComposePost: () -> Unit,
  private val onOpenPost: (ThreadProps) -> Unit,
  private val onOpenUser: (UserReference) -> Unit,
  private val onOpenImage: (OpenImageAction) -> Unit,
  private val onReplyToPost: (PostReplyInfo) -> Unit,
  private val onExit: () -> Unit,
) : ViewRendering by screen({
  val feedState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  Surface(modifier = Modifier.onBackPressed(onExit)) {
    Scaffold(
      contentWindowInsets = WindowInsets(0.dp),
      topBar = {
        CenterAlignedTopAppBar(
          windowInsets = WindowInsets(0.dp),
          navigationIcon = {
            Box(Modifier.padding(start = 12.dp)) {
              AvatarImage(
                modifier = Modifier.size(32.dp),
                avatarUrl = profile?.avatar,
                onClick = { profile?.did?.let { onOpenUser(UserReference.Did(it)) } },
                contentDescription = profile?.displayName ?: profile?.handle,
                fallbackColor = profile?.handle?.color() ?: Color.Black,
              )
            }
          },
          title = {
            Text(
              modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                  coroutineScope.launch {
                    if (feedState.isScrollInProgress) {
                      // If scrolling already (either via animateScrollToItem or fling), snap to top.
                      feedState.scrollToItem(0)
                    } else {
                      // Otherwise, smoothly scroll to the top.
                      feedState.animateScrollToItem(0)
                    }
                  }
                },
              ),
              text = "OZONE",
              style = MaterialTheme.typography.titleLarge.copy(fontFamily = heroFont),
            )
          },
          actions = {
            IconButton(onClick = {
              coroutineScope.launch {
                feedState.scrollToItem(0)
              }
              onRefresh()
            }) {
              Icon(
                painter = rememberVectorPainter(Icons.Default.Refresh),
                contentDescription = "Refresh",
              )
            }
          }
        )
      },
      floatingActionButton = {
        if (showComposePostButton) {
          FloatingActionButton(onClick = onComposePost) {
            Icon(
              painter = rememberVectorPainter(Icons.Default.ChatBubbleOutline),
              contentDescription = "Compose",
            )
          }
        }
      },
      floatingActionButtonPosition = FabPosition.End,
    ) { contentPadding ->
      InfiniteListHandler(feedState, buffer = 10, onLoadMore = onLoadMore)

      Box(modifier = Modifier.padding(contentPadding).fillMaxSize()) {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          state = feedState,
        ) {
          items(items = timeline) { post ->
            key(post) {
              Divider(thickness = Dp.Hairline)

              TimelinePostItem(
                now = now,
                post = post,
                onOpenPost = onOpenPost,
                onOpenUser = onOpenUser,
                onOpenImage = onOpenImage,
                onReplyToPost = { onReplyToPost(post.asReplyInfo()) },
              )
            }
          }

          item {
            Divider(thickness = Dp.Hairline)
          }
        }

        AnimatedVisibility(
          showRefreshPrompt,
          enter = fadeIn() + slideInVertically(),
          exit = fadeOut() + slideOutVertically(),
        ) {
          Box(modifier = Modifier.fillMaxWidth()) {
            Button(
              modifier = Modifier.align(Alignment.TopCenter),
              onClick = {
                coroutineScope.launch {
                  feedState.scrollToItem(0)
                }
                onRefresh()
              },
            ) {
              Text("Load Newest Posts")
            }
          }
        }
      }
    }
  }
})
