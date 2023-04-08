package sh.christian.ozone.timeline.components

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import sh.christian.ozone.model.Profile
import kotlin.time.Duration

@Composable
internal fun PostHeadline(
  now: Instant,
  createdAt: Instant,
  author: Profile,
) {
  Row(horizontalArrangement = spacedBy(4.dp)) {
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
