package sh.christian.ozone.timeline.components.feature

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import sh.christian.ozone.model.LitePost
import sh.christian.ozone.model.Profile
import sh.christian.ozone.timeline.components.PostHeadline
import sh.christian.ozone.ui.compose.AvatarImage
import sh.christian.ozone.util.color

@Composable
fun VisiblePostPost(
  now: Instant,
  post: LitePost,
  author: Profile,
  onClick: () -> Unit,
) {
  FeatureContainer(onClick = onClick) {
    Row(horizontalArrangement = spacedBy(8.dp)) {
      AvatarImage(
        modifier = Modifier.size(16.dp).align(Alignment.CenterVertically),
        avatarUrl = author.avatar,
        onClick = null,
        contentDescription = author.displayName ?: author.handle,
        fallbackColor = author.handle.color(),
      )

      PostHeadline(now, post.createdAt, author)
    }
    Text(
      text = post.text,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
