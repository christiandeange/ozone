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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.bsky.actor.RefWithInfo
import app.bsky.feed.FeedViewPostReplyRef
import app.bsky.feed.Post
import app.bsky.feed.PostView
import app.bsky.feed.PostViewEmbedUnion.ImagesPresented
import io.kamel.image.lazyPainterResource
import kotlinx.datetime.Instant
import sh.christian.ozone.user.UserReference
import sh.christian.ozone.user.UserReference.Did
import sh.christian.ozone.user.UserReference.Handle
import sh.christian.ozone.ui.compose.AvatarImage
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.ui.compose.PostImage
import sh.christian.ozone.ui.icons.ChatBubbleOutline
import sh.christian.ozone.ui.icons.Repeat
import sh.christian.ozone.ui.icons.Reply
import sh.christian.ozone.util.color
import sh.christian.ozone.util.deserialize
import sh.christian.ozone.util.isUrl
import sh.christian.ozone.util.recordType
import kotlin.time.Duration

@Composable
fun TimelinePost(
  now: Instant,
  postView: PostView,
  replyRef: FeedViewPostReplyRef?,
  onOpenUser: (UserReference) -> Unit,
  onOpenImage: (OpenImageAction) -> Unit,
) {
  Row(
    modifier = Modifier.padding(16.dp),
    horizontalArrangement = spacedBy(16.dp),
  ) {
    val author: RefWithInfo = postView.author
    AvatarImage(
      modifier = Modifier.size(48.dp),
      avatarUrl = author.avatar,
      onClick = { onOpenUser(Handle(author.handle)) },
      contentDescription = author.displayName ?: author.handle,
      fallbackColor = author.handle.color(),
    )

    Column(Modifier.weight(1f)) {
      when (postView.record.recordType) {
        "app.bsky.feed.post" -> {
          val post = Post.serializer().deserialize(postView.record)
          PostHeadline(now, post, author)
          PostReplyLine(replyRef, onOpenUser)
          Column(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalArrangement = spacedBy(4.dp),
          ) {
            PostText(post, onOpenUser)
            PostImages(postView, onOpenImage)
          }
          PostActions(postView)
        }
        else -> {
          Text(
            modifier = Modifier.padding(32.dp),
            text = "Unknown record type: ${postView.record.recordType}",
          )
        }
      }
    }
  }
}

@Composable
private fun PostHeadline(
  now: Instant,
  post: Post,
  author: RefWithInfo,
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

    val postTime = Instant.parse(post.createdAt)
    val timeDelta: Duration = now - postTime

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
  replyRef: FeedViewPostReplyRef?,
  onOpenUser: (UserReference) -> Unit,
) {
  replyRef?.parent?.author?.let { original ->
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
  post: Post,
  onOpenUser: (UserReference) -> Unit,
) {
  if (post.text.isNotBlank()) {
    val postText = remember(post.text) {
      buildAnnotatedString {
        append(post.text)

        post.entities.forEach { entity ->
          addStyle(
            style = SpanStyle(color = Color(0xFF3B62FF)),
            start = entity.index.start.toInt(),
            end = entity.index.end.toInt(),
          )

          addStringAnnotation(
            tag = "clickable",
            annotation = entity.value,
            start = entity.index.start.toInt(),
            end = entity.index.end.toInt(),
          )
        }
      }
    }

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
  postView: PostView,
  onOpenImage: (OpenImageAction) -> Unit,
) {
  (postView.embed as? ImagesPresented)?.value?.images?.takeIf { it.isNotEmpty() }?.let { images ->
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
private fun PostActions(postView: PostView) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = SpaceBetween,
  ) {
    PostAction(
      icon = Icons.Default.ChatBubbleOutline,
      contentDescription = "Reply",
      text = postView.replyCount.toString(),
    )
    PostAction(
      icon = Icons.Default.Repeat,
      contentDescription = "Repost",
      text = postView.repostCount.toString(),
      tint = if (postView.viewer.repost != null) {
        Color.Green
      } else {
        MaterialTheme.colorScheme.outline
      },
    )
    PostAction(
      icon = if (postView.viewer.upvote != null) {
        Icons.Default.Favorite
      } else {
        Icons.Default.FavoriteBorder
      },
      contentDescription = "Like",
      text = (postView.upvoteCount - postView.downvoteCount).toString(),
      tint = if (postView.viewer.upvote != null) {
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
