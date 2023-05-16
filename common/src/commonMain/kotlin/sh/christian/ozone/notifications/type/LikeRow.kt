package sh.christian.ozone.notifications.type

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import sh.christian.ozone.notifications.NotificationRowContext
import sh.christian.ozone.thread.ThreadProps
import sh.christian.ozone.ui.compose.TimeDelta

@Composable
fun LikeRow(
  context: NotificationRowContext,
  notification: Notification,
  content: Notification.Content.Liked,
) {
  val profile = notification.author
  NotificationRowScaffold(
    modifier = Modifier.clickable { context.onOpenPost(ThreadProps.FromPost(content.post)) },
    context = context,
    profile = profile,
    icon = {
      Icon(
        painter = rememberVectorPainter(Icons.Default.Favorite),
        tint = Color.Red,
        contentDescription = "Liked your post",
      )
    },
    content = {
      Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          val profileText = profile.displayName ?: "@${profile.handle}"
          Text(
            modifier = Modifier.alignByBaseline(),
            text = "$profileText liked your post",
          )

          TimeDelta(
            modifier = Modifier.alignByBaseline(),
            duration = context.now - notification.indexedAt,
          )
        }
        Text(
          text = content.post.text,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.outline),
        )
      }
    },
  )
}
