package sh.christian.ozone.notifications.type

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import sh.christian.ozone.model.Notification
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.notifications.NotificationRowContext
import sh.christian.ozone.thread.ThreadProps
import sh.christian.ozone.ui.compose.TimeDelta
import sh.christian.ozone.ui.icons.Repeat

@Composable
fun RepostRow(
  context: NotificationRowContext,
  notification: Notification,
  content: Notification.Content.Reposted,
) {
  RepostRow(context, notification, content.post)
}

@Composable
fun RepostRow(
  context: NotificationRowContext,
  notification: Notification,
  content: Notification.Content.RepostedViaRepost,
) {
  RepostRow(context, notification, content.post)
}

@Composable
fun RepostRow(
  context: NotificationRowContext,
  notification: Notification,
  post: TimelinePost,
) {
  val profile = notification.author
  NotificationRowScaffold(
    modifier = Modifier.clickable { context.onOpenPost(ThreadProps.FromPost(post)) },
    context = context,
    profile = profile,
    icon = {
      Icon(
        painter = rememberVectorPainter(Icons.Default.Repeat),
        tint = Color.Green,
        contentDescription = "Reposted your post",
      )
    },
    content = {
      Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          val profileText = profile.displayName ?: "@${profile.handle}"
          Text(
            modifier = Modifier.alignByBaseline(),
            text = "$profileText reposted your post",
          )

          TimeDelta(
            modifier = Modifier.alignByBaseline(),
            delta = context.now - notification.indexedAt,
          )
        }
        Text(
          text = post.text,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.outline),
        )
      }
    },
  )
}
