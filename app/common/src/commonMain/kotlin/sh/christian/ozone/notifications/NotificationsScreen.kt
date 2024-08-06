package sh.christian.ozone.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import sh.christian.ozone.compose.PostReplyInfo
import sh.christian.ozone.model.Moment
import sh.christian.ozone.model.Notification
import sh.christian.ozone.notifications.type.FollowRow
import sh.christian.ozone.notifications.type.JoinedStarterPackRow
import sh.christian.ozone.notifications.type.LikeRow
import sh.christian.ozone.notifications.type.MentionRow
import sh.christian.ozone.notifications.type.QuoteRow
import sh.christian.ozone.notifications.type.ReplyRow
import sh.christian.ozone.notifications.type.RepostRow
import sh.christian.ozone.thread.ThreadProps
import sh.christian.ozone.ui.compose.InfiniteListHandler
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.ui.compose.heroFont
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen
import sh.christian.ozone.user.UserReference

@OptIn(ExperimentalFoundationApi::class)
class NotificationsScreen(
  private val now: Moment,
  private val notifications: ImmutableList<Notification>,
  private val onLoadMore: () -> Unit,
  private val onExit: () -> Unit,
  private val onOpenPost: (ThreadProps) -> Unit,
  private val onOpenUser: (UserReference) -> Unit,
  private val onOpenImage: (OpenImageAction) -> Unit,
  private val onReplyToPost: (PostReplyInfo) -> Unit,
) : ViewRendering by screen({
  val state = rememberLazyListState()
  val context = remember {
    NotificationRowContext(now, onOpenPost, onOpenUser, onOpenImage, onReplyToPost)
  }

  Surface(modifier = Modifier.onBackPressed(onExit)) {
    Scaffold(
      contentWindowInsets = WindowInsets(0.dp),
      topBar = {
        CenterAlignedTopAppBar(
          windowInsets = WindowInsets(0.dp),
          title = {
            Text(
              text = "Notifications",
              style = MaterialTheme.typography.titleLarge.copy(fontFamily = heroFont()),
            )
          },
        )
      },
    ) { contentPadding ->
      InfiniteListHandler(state, buffer = 10, onLoadMore = onLoadMore)

      LazyColumn(
        modifier = Modifier.padding(contentPadding).fillMaxSize(),
        state = state,
      ) {
        stickyHeader {
          HorizontalDivider(thickness = Dp.Hairline)
        }

        items(notifications) { notification ->
          key(notification) {
            Column {
              val unreadModifier = if (notification.isRead) {
                Modifier
              } else {
                Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
              }

              Box(
                modifier = Modifier.fillMaxWidth().then(unreadModifier),
                propagateMinConstraints = true,
              ) {
                when (val content = notification.content) {
                  is Notification.Content.Followed -> FollowRow(context, notification)
                  is Notification.Content.Liked -> LikeRow(context, notification, content)
                  is Notification.Content.Mentioned -> MentionRow(context, content)
                  is Notification.Content.Quoted -> QuoteRow(context, content)
                  is Notification.Content.RepliedTo -> ReplyRow(context, content)
                  is Notification.Content.Reposted -> RepostRow(context, notification, content)
                  is Notification.Content.JoinedStarterPack -> JoinedStarterPackRow(context, notification)
                  null -> Unit
                }
              }
              if (notification.content != null) {
                HorizontalDivider(thickness = Dp.Hairline)
              }
            }
          }
        }
      }
    }
  }
})

data class NotificationRowContext(
  val now: Moment,
  val onOpenPost: (ThreadProps) -> Unit,
  val onOpenUser: (UserReference) -> Unit,
  val onOpenImage: (OpenImageAction) -> Unit,
  val onReplyToPost: (PostReplyInfo) -> Unit,
)
