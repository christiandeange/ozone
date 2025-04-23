package sh.christian.ozone.timeline.components

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import sh.christian.ozone.model.Moment
import sh.christian.ozone.model.Profile
import sh.christian.ozone.ui.compose.TimeDelta
import sh.christian.ozone.ui.compose.VerifiedCheck

@Composable
internal fun PostHeadline(
  now: Moment,
  createdAt: Moment,
  author: Profile,
) {
  Row(horizontalArrangement = spacedBy(4.dp)) {
    val primaryText = author.displayName ?: author.handle.handle
    val secondaryText = author.handle.handle.takeUnless { it == primaryText }

    Text(
      modifier = Modifier.alignByBaseline(),
      text = primaryText,
      maxLines = 1,
      style = LocalTextStyle.current.copy(fontWeight = Bold),
    )

    VerifiedCheck(
      modifier = Modifier.align(Alignment.CenterVertically),
      verification = author.verification,
    )

    if (secondaryText != null) {
      Text(
        modifier = Modifier.alignByBaseline().weight(1f, fill = false),
        text = author.handle.handle,
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

    TimeDelta(
      modifier = Modifier.alignByBaseline(),
      delta = now - createdAt,
    )
  }
}
