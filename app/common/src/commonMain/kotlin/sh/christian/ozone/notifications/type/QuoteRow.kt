package sh.christian.ozone.notifications.type

import androidx.compose.runtime.Composable
import sh.christian.ozone.compose.asReplyInfo
import sh.christian.ozone.model.Notification
import sh.christian.ozone.notifications.NotificationRowContext
import sh.christian.ozone.timeline.components.TimelinePostItem

@Composable
fun QuoteRow(
  context: NotificationRowContext,
  content: Notification.Content.Quoted,
) {
  TimelinePostItem(
    now = context.now,
    post = content.post,
    onOpenPost = context.onOpenPost,
    onOpenUser = context.onOpenUser,
    onOpenImage = context.onOpenImage,
    onReplyToPost = { context.onReplyToPost(content.post.asReplyInfo()) },
  )
}
