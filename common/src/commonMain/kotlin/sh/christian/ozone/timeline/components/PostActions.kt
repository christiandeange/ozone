package sh.christian.ozone.timeline.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.ui.icons.ChatBubbleOutline
import sh.christian.ozone.ui.icons.Repeat

@Composable
internal fun PostActions(post: TimelinePost) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = SpaceBetween,
  ) {
    PostAction(
      icon = Icons.Default.ChatBubbleOutline,
      contentDescription = "Reply",
      text = post.replyCount.toString(),
    )
    PostAction(
      icon = Icons.Default.Repeat,
      contentDescription = "Repost",
      text = post.repostCount.toString(),
      tint = if (post.reposted) {
        Color.Green
      } else {
        MaterialTheme.colorScheme.outline
      },
    )
    PostAction(
      icon = if (post.liked) {
        Icons.Default.Favorite
      } else {
        Icons.Default.FavoriteBorder
      },
      contentDescription = "Like",
      text = post.likeCount.toString(),
      tint = if (post.liked) {
        Color.Red
      } else {
        MaterialTheme.colorScheme.outline
      },
    )
    Spacer(Modifier.width(0.dp))
  }
}

@Composable
private fun PostAction(
  icon: ImageVector,
  contentDescription: String,
  text: String?,
  tint: Color = MaterialTheme.colorScheme.outline,
) {
  Row(
    modifier = Modifier
      .padding(vertical = 4.dp)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(bounded = false),
        onClick = {},
      ),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = spacedBy(4.dp),
  ) {
    Icon(
      modifier = Modifier.size(16.dp),
      painter = rememberVectorPainter(icon),
      contentDescription = contentDescription,
      tint = tint,
    )

    if (text != null) {
      Text(
        text = text,
        maxLines = 1,
        style = MaterialTheme.typography.bodySmall.copy(color = tint),
      )
    }
  }
}
