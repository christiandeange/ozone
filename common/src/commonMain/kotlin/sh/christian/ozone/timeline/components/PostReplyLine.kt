package sh.christian.ozone.timeline.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import sh.christian.ozone.model.Profile
import sh.christian.ozone.ui.icons.Reply
import sh.christian.ozone.user.UserReference

@Composable
internal fun PostReplyLine(
  replyingTo: Profile?,
  onOpenUser: (UserReference) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (replyingTo != null) {
    Row(
      modifier = modifier.clickable { onOpenUser(UserReference.Did(replyingTo.did)) },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = spacedBy(4.dp),
    ) {
      Icon(
        modifier = Modifier.size(12.dp),
        painter = rememberVectorPainter(Icons.Default.Reply),
        contentDescription = "Reply",
        tint = MaterialTheme.typography.bodySmall.color,
      )

      Text(
        text = "Reply to ${replyingTo.displayName ?: replyingTo.handle}",
        maxLines = 1,
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}
