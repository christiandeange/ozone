package sh.christian.ozone.timeline.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
import sh.christian.ozone.util.format

@Composable
fun TimelinePostItem(
  modifier: Modifier = Modifier,
  now: Moment,
  post: TimelinePost,
  onOpenPost: (ThreadProps) -> Unit,
  onOpenUser: (UserReference) -> Unit,
  onOpenImage: (OpenImageAction) -> Unit,
  onReplyToPost: () -> Unit,
) {
  Row(
    modifier = modifier
      .clickable { onOpenPost(ThreadProps.FromPost(post)) }
      .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
    horizontalArrangement = spacedBy(16.dp),
  ) {
    val author: Profile = post.author
    AvatarImage(
      modifier = Modifier.size(48.dp),
      avatarUrl = author.avatar,
      onClick = { onOpenUser(UserReference.Did(author.did)) },
      contentDescription = author.displayName ?: author.handle.handle,
      fallbackColor = author.handle.color(),
    )

    Column(Modifier.weight(1f)) {
      PostHeadline(now, post.createdAt, author)
      PostReasonLine(post.reason, onOpenUser)
      PostReplyLine(post.reply?.parent?.author, onOpenUser)
      Column(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalArrangement = spacedBy(8.dp),
      ) {
        PostText(post, { onOpenPost(ThreadProps.FromPost(post)) }, onOpenUser)
        PostFeature(now, post.feature, onOpenImage, onOpenPost)
      }
      PostActions(
        replyCount = format(post.replyCount),
        repostCount = format(post.repostCount),
        likeCount = format(post.likeCount),
        reposted = post.reposted,
        liked = post.liked,
        iconSize = 16.dp,
        onReplyToPost = onReplyToPost,
      )
    }
  }
}
