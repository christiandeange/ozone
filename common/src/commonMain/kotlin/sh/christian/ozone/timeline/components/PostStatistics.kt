package sh.christian.ozone.timeline.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.ui.compose.Statistic

@Composable
internal fun PostStatistics(
  post: TimelinePost,
  onReplyToPost: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    val replies = post.replyCount
    val reposts = post.repostCount
    val likes = post.likeCount

    val noOpHandler = { /* TODO */ }

    when (replies) {
      0L -> Unit
      1L -> Statistic(replies, "reply", onReplyToPost)
      else -> Statistic(replies, "replies", onReplyToPost)
    }

    when (reposts) {
      0L -> Unit
      1L -> Statistic(reposts, "repost", noOpHandler)
      else -> Statistic(reposts, "reposts", noOpHandler)
    }

    when (likes) {
      0L -> Unit
      1L -> Statistic(likes, "like", noOpHandler)
      else -> Statistic(likes, "likes", noOpHandler)
    }
  }
}
