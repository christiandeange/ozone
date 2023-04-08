package sh.christian.ozone.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import sh.christian.ozone.model.FullProfile
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.timeline.components.TimelinePostItem
import sh.christian.ozone.ui.compose.AvatarImage
import sh.christian.ozone.ui.compose.BannerImage
import sh.christian.ozone.ui.compose.InfiniteListHandler
import sh.christian.ozone.ui.compose.OpenImageAction
import sh.christian.ozone.ui.compose.OverImageIconButton
import sh.christian.ozone.ui.compose.SystemInsets
import sh.christian.ozone.ui.compose.foreground
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.compose.rememberSystemInsets
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen
import sh.christian.ozone.user.UserReference
import sh.christian.ozone.util.color
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
class ProfileScreen(
  private val now: Instant,
  private val profile: FullProfile,
  private val feed: List<TimelinePost>,
  private val isSelf: Boolean,
  private val onLoadMore: () -> Unit,
  private val onOpenUser: (UserReference) -> Unit,
  private val onOpenImage: (OpenImageAction) -> Unit,
  private val onExit: () -> Unit,
) : ViewRendering by screen({
  val density = LocalDensity.current.density
  val state = rememberLazyListState()

  val transitionPercentage by remember {
    derivedStateOf {
      if (state.layoutInfo.visibleItemsInfo.isNotEmpty() && state.firstVisibleItemIndex == 0) {
        val firstItemInfo = state.layoutInfo.visibleItemsInfo.first()
        state.firstVisibleItemScrollOffset / firstItemInfo.size.toFloat()
      } else {
        1f
      }
    }
  }

  var headerHeight by remember { mutableStateOf(0) }
  val insets = rememberSystemInsets()
  val firstItemTranslationY by remember {
    derivedStateOf {
      transitionPercentage * (headerHeight - (insets.calculateTopPadding().value + 48f) * density)
    }
  }
  val firstItemOverlayColor by remember {
    derivedStateOf {
      Color.Black.copy(alpha = transitionPercentage * 0.62f)
    }
  }

  Surface(modifier = Modifier.fillMaxSize().onBackPressed(onExit)) {
    InfiniteListHandler(state, buffer = 10, onLoadMore = onLoadMore)

    LazyColumn(state = state) {
      stickyHeader(contentType = "banner") {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { headerHeight = it.size.height },
        ) {
          BannerImage(
            modifier = Modifier
              .blur((transitionPercentage * 4).dp)
              .graphicsLayer { translationY = -firstItemTranslationY }
              .foreground(firstItemOverlayColor),
            imageUrl = profile.banner,
            contentDescription = null,
          )

          val avatarUrl = profile.avatar
          AvatarImage(
            modifier = Modifier
              .padding(start = 12.dp, end = 16.dp)
              .align(Alignment.BottomStart)
              .size(80.dp)
              .graphicsLayer {
                alpha = max(1f - transitionPercentage * 2, 0f)
                translationY = 40 * density - firstItemTranslationY
              },
            avatarUrl = avatarUrl,
            contentDescription = profile.displayName ?: profile.handle,
            fallbackColor = profile.handle.color(),
            onClick = {
              onOpenImage(OpenImageAction(avatarUrl!!, profile.handle))
            }.takeIf { avatarUrl != null },
          )

          SystemInsets {
            OverImageIconButton(onClick = onExit) {
              Icon(
                painter = rememberVectorPainter(Icons.Default.ArrowBack),
                contentDescription = "Back",
              )
            }
          }
        }
      }

      item(contentType = "profile_header") {
        Box {
          var isFollowing by remember { mutableStateOf(profile.followingMe) }

          if (isSelf) {
            FollowButton(
              modifier = Modifier.align(Alignment.TopEnd),
              buttonColors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.outlineVariant,
              ),
              textTint = MaterialTheme.colorScheme.onSurface,
              icon = rememberVectorPainter(Icons.Default.Edit),
              text = "Edit Profile",
              onClick = { /* TODO */ },
            )
          } else if (isFollowing) {
            FollowButton(
              modifier = Modifier.align(Alignment.TopEnd),
              buttonColors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.outlineVariant,
              ),
              textTint = MaterialTheme.colorScheme.onSurface,
              icon = rememberVectorPainter(Icons.Default.Check),
              text = "Following",
              onClick = { isFollowing = !isFollowing },
            )
          } else {
            FollowButton(
              modifier = Modifier.align(Alignment.TopEnd),
              buttonColors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
              ),
              textTint = MaterialTheme.colorScheme.inverseOnSurface,
              icon = rememberVectorPainter(Icons.Default.Add),
              text = "Follow",
              onClick = { isFollowing = !isFollowing },
            )
          }

          Column(
            modifier = Modifier
              .padding(top = 40.dp)
              .padding(horizontal = 16.dp),
          ) {
            Text(
              modifier = Modifier.padding(top = 8.dp),
              text = profile.displayName.orEmpty(),
              style = MaterialTheme.typography.displaySmall,
            )

            Row(horizontalArrangement = spacedBy(8.dp)) {
              if (profile.followingMe) {
                Text(
                  modifier = Modifier
                    .alignByBaseline()
                    .background(
                      MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraSmall
                    )
                    .padding(4.dp),
                  text = "Follows you",
                  style = MaterialTheme.typography.labelSmall,
                )
              }

              Text(
                modifier = Modifier.alignByBaseline(),
                text = "@${profile.handle}",
                style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.outline),
              )
            }

            ProfileStats(
              followers = profile.followersCount,
              following = profile.followsCount,
              posts = profile.postsCount,
            )

            profile.description.takeIf { !it.isNullOrBlank() }?.let { description ->
              Text(
                modifier = Modifier.fillMaxWidth(),
                text = description,
              )
            }
          }
        }
      }

      items(items = feed) { post ->
        TimelinePostItem(
          now = now,
          post = post,
          onOpenUser = onOpenUser,
          onOpenImage = onOpenImage,
        )
      }
    }
  }
})

@Composable
private fun FollowButton(
  modifier: Modifier = Modifier,
  buttonColors: ButtonColors,
  textTint: Color,
  icon: Painter,
  text: String,
  onClick: () -> Unit,
) {
  Button(
    modifier = modifier.padding(8.dp),
    onClick = onClick,
    colors = buttonColors
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = spacedBy(4.dp),
    ) {
      Icon(
        modifier = Modifier.size(16.dp),
        painter = icon,
        contentDescription = text,
        tint = textTint,
      )

      Text(
        text = text,
        maxLines = 1,
        style = LocalTextStyle.current.copy(color = textTint),
      )
    }
  }
}

@Composable
private fun ProfileStats(
  followers: Long,
  following: Long,
  posts: Long,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = spacedBy(8.dp),
  ) {
    ProfileStat(followers, "followers", { /* TODO */ })
    ProfileStat(following, "following", { /* TODO */ })
    ProfileStat(posts, "posts", { /* TODO */ })
  }
}

@Composable
private fun ProfileStat(
  value: Long,
  description: String,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier.clickable(onClick = onClick).padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = spacedBy(2.dp),
  ) {
    Text(
      modifier = Modifier.alignByBaseline(),
      text = format(value),
      maxLines = 1,
      style = LocalTextStyle.current.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = Bold,
      ),
    )
    Text(
      modifier = Modifier.alignByBaseline(),
      text = description,
      maxLines = 1,
      style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.outline),
    )
  }
}

private fun format(value: Long): String {
  return when (value) {
    in 10_000_000..Long.MAX_VALUE -> format(value, 1_000_000f, "M", wholeNumber = true)
    in 1_000_000..10_000_000 -> format(value, 1_000_000f, "M")
    in 100_000..1_000_000 -> format(value, 1_000f, "K", wholeNumber = true)
    in 10_000..1_000_000 -> format(value, 1_000f, "K")
    else -> value.toString()
  }
}

private fun format(
  value: Long,
  div: Float,
  suffix: String,
  wholeNumber: Boolean = false,
): String {
  val msd = (value / div).toInt()
  val lsd = (value * 10 / div).toInt() % 10

  return if (lsd == 0 || wholeNumber) {
    "$msd$suffix"
  } else {
    "$msd.$lsd$suffix"
  }
}
