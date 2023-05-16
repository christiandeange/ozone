package sh.christian.ozone.notifications.type

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import sh.christian.ozone.model.Notification
import sh.christian.ozone.notifications.NotificationRowContext
import sh.christian.ozone.ui.compose.TimeDelta
import sh.christian.ozone.user.UserReference

@Composable
fun FollowRow(
  context: NotificationRowContext,
  notification: Notification,
  content: Notification.Content.Followed,
) {
  val profile = notification.author
  NotificationRowScaffold(
    modifier = Modifier.clickable { context.onOpenUser(UserReference.Did(profile.did)) },
    context = context,
    profile = profile,
    icon = {
      Icon(
        painter = rememberVectorPainter(Icons.Default.Person),
        tint = MaterialTheme.colorScheme.primary,
        contentDescription = "Followed you",
      )
    },
    content = {
      Row(horizontalArrangement = spacedBy(8.dp)) {
        val profileText = profile.displayName ?: "@${profile.handle}"
        Text(
          modifier = Modifier.alignByBaseline(),
          text = "$profileText followed you",
        )
        TimeDelta(
          modifier = Modifier.alignByBaseline(),
          duration = context.now - notification.indexedAt,
        )
      }
    },
  )
}
