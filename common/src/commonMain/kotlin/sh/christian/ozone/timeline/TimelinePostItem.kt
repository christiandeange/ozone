package sh.christian.ozone.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.kamel.image.lazyPainterResource
import kotlinx.datetime.Instant
import sh.christian.ozone.model.Profile
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.model.TimelinePostFeature.ImagesFeature
import sh.christian.ozone.model.TimelinePostLink
import sh.christian.ozone.model.TimelinePostReply
import sh.christian.ozone.ui.compose.AvatarImage
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.ui.compose.PostImage
import sh.christian.ozone.ui.icons.ChatBubbleOutline
import sh.christian.ozone.ui.icons.Repeat
import sh.christian.ozone.ui.icons.Reply
import sh.christian.ozone.user.UserReference
import sh.christian.ozone.user.UserReference.Did
import sh.christian.ozone.user.UserReference.Handle
import sh.christian.ozone.util.color
import sh.christian.ozone.util.isUrl
import kotlin.time.Duration

@Composable
fun TimelinePostItem(
  now: Instant,
  post: TimelinePost,
  onOpenUser: (UserReference) -> Unit,
  onOpenImage: (OpenImageAction) -> Unit,
) {
  Row(
    modifier = Modifier.padding(16.dp),
    horizontalArrangement = spacedBy(16.dp),
  ) {
    val author: Profile = post.author
    AvatarImage(
      modifier = Modifier.size(48.dp),
      avatarUrl = author.avatar,
      onClick = { onOpenUser(Handle(author.handle)) },
      contentDescription = author.displayName ?: author.handle,
      fallbackColor = author.handle.color(),
    )

    Column(Modifier.weight(1f)) {
      PostHeadline(now, post.createdAt, author)
      PostReplyLine(post.reply, onOpenUser)
      Column(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalArrangement = spacedBy(4.dp),
      ) {
        PostText(post, onOpenUser)
        PostImages(post.feature as? ImagesFeature, onOpenImage)
      }
      PostActions(post)
    }
  }
}

@Composable
private fun PostHeadline(
  now: Instant,
  createdAt: Instant,
  author: Profile,
) {
  Row(
    horizontalArrangement = spacedBy(4.dp),
  ) {
    val primaryText = author.displayName ?: author.handle
    val secondaryText = author.handle.takeUnless { it == primaryText }

    Text(
      modifier = Modifier.alignByBaseline(),
      text = primaryText,
      maxLines = 1,
      style = LocalTextStyle.current.copy(fontWeight = Bold),
    )

    if (secondaryText != null) {
      Text(
        modifier = Modifier.alignByBaseline().weight(1f, fill = false),
        text = author.handle,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = MaterialTheme.typography.bodySmall,
      )

      Text(
        modifier = Modifier.alignByBaseline(),
        text = "•",
        maxLines = 1,
        style = MaterialTheme.typography.bodySmall,
      )
    }

    val timeDelta: Duration = now - createdAt
    Text(
      modifier = Modifier.alignByBaseline(),
      text = timeDelta.toComponents { days, hours, minutes, seconds, _ ->
        when {
          days > 0 -> "${days}d"
          hours > 0 -> "${hours}h"
          minutes > 0 -> "${minutes}m"
          seconds > 0 -> "${seconds}s"
          seconds < 0 || minutes < 0 || hours < 0 || days < 0 -> "The Future"
          else -> "Now"
        }
      },
      maxLines = 1,
      style = MaterialTheme.typography.bodySmall,
    )
  }
}

@Composable
private fun PostReplyLine(
  reply: TimelinePostReply?,
  onOpenUser: (UserReference) -> Unit,
) {
  reply?.parent?.author?.let { original ->
    Row(
      modifier = Modifier.clickable { onOpenUser(Handle(original.handle)) },
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
        text = "Reply to ${original.displayName ?: original.handle}",
        maxLines = 1,
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}

@Composable
private fun PostText(
  post: TimelinePost,
  onOpenUser: (UserReference) -> Unit,
) {
  if (post.text.isNotBlank()) {
    val postText = formatTextPost(post.text, post.textLinks)

    val uriHandler = LocalUriHandler.current
    ClickableText(
      text = postText,
      style = LocalTextStyle.current.copy(color = LocalContentColor.current),
      onClick = { index ->
        postText.getStringAnnotations("clickable", index, index)
          .firstOrNull()
          ?.item
          ?.let { target ->
            if (target.startsWith("did:")) {
              onOpenUser(Did(target))
            } else if (target.isUrl()) {
              uriHandler.openUri(target)
            } else if (target.startsWith("#")) {
              println("Clicked on hashtag $target")
            } else {
              println("Clicked on unknown target: $target")
            }
          }
      },
    )
  }
}

@Composable
private fun PostImages(
  feature: ImagesFeature?,
  onOpenImage: (OpenImageAction) -> Unit,
) {
  feature?.images?.takeIf { it.isNotEmpty() }?.let { images ->
    Row(horizontalArrangement = spacedBy(8.dp)) {
      images.forEach { image ->
        var size by remember(image) { mutableStateOf(IntSize.Zero) }
        var position by remember(image) { mutableStateOf(Offset.Zero) }

        // Load the full-size image into the cache.
        lazyPainterResource(image.fullsize)

        PostImage(
          modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .onGloballyPositioned { coordinates ->
              size = coordinates.size
              position = coordinates.positionInRoot()
            },
          imageUrl = image.thumb,
          contentDescription = image.alt,
          onClick = {
            onOpenImage(OpenImageAction(image.fullsize, image.alt))
          },
          fallbackColor = Color.Gray,
        )
      }
    }
  }
}

@Composable
private fun PostActions(post: TimelinePost) {
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

@Composable
fun formatTextPost(
  text: String,
  textLinks: List<TimelinePostLink>,
): AnnotatedString {
  return remember(text, textLinks) {
    buildAnnotatedString {
      append(text)

      textLinks.forEach { link ->
        addStyle(
          style = SpanStyle(color = Color(0xFF3B62FF)),
          start = link.start,
          end = link.end,
        )

        addStringAnnotation(
          tag = "clickable",
          annotation = link.value,
          start = link.start,
          end = link.end,
        )
      }
    }
  }
}
