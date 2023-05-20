package sh.christian.ozone.timeline.components

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.christian.ozone.model.Moment
import sh.christian.ozone.model.Profile
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.thread.ThreadProps
import sh.christian.ozone.ui.compose.AvatarImage
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.user.UserReference
import sh.christian.ozone.util.color

@Composable
fun ThreadPostItem(
  modifier: Modifier = Modifier,
  now: Moment,
  post: TimelinePost,
  onOpenUser: (UserReference) -> Unit,
  onOpenImage: (OpenImageAction) -> Unit,
  onOpenPost: (ThreadProps) -> Unit,
  onReplyToPost: () -> Unit,
) {
  Column(modifier = modifier.padding(top = 16.dp)) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp),
      horizontalArrangement = spacedBy(16.dp),
    ) {
      val author: Profile = post.author
      AvatarImage(
        modifier = Modifier.size(48.dp),
        avatarUrl = author.avatar,
        onClick = { onOpenUser(UserReference.Did(author.did)) },
        contentDescription = author.displayName ?: author.handle,
        fallbackColor = author.handle.color(),
      )

      Column(Modifier.weight(1f)) {
        PostHeadline(now, post.createdAt, author)
        PostReasonLine(post.reason, onOpenUser)
        Column(
          modifier = Modifier.padding(bottom = 8.dp),
          verticalArrangement = spacedBy(8.dp),
        ) {
          CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.headlineSmall) {
            PostText(post, {}, onOpenUser)
          }
          PostFeature(now, post.feature, onOpenImage, onOpenPost)
          PostDate(post.createdAt)
        }
      }
    }

    Column {
      if (post.hasInteractions()) {
        Divider(thickness = 1.dp)

        Box(modifier = Modifier.padding(start = 80.dp)) {
          PostStatistics(
            post = post,
            onReplyToPost = onReplyToPost,
          )
        }
      }

      Divider(thickness = 1.dp)

      Box(modifier = Modifier.padding(start = 80.dp, top = 8.dp, bottom = 8.dp)) {
        PostActions(
          replyCount = null,
          repostCount = null,
          likeCount = null,
          reposted = post.reposted,
          liked = post.liked,
          iconSize = 24.dp,
          onReplyToPost = onReplyToPost,
        )
      }

      Divider(thickness = 1.dp)
    }
  }
}

fun TimelinePost.hasInteractions(): Boolean {
  return replyCount > 0 || repostCount > 0 || likeCount > 0
}
