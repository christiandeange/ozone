package sh.christian.ozone.timeline.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.datetime.Instant
import sh.christian.ozone.util.formatDate
import sh.christian.ozone.util.formatTime

@Composable
fun PostDate(time: Instant) {
  Text(
    text = "${time.formatDate()} • ${time.formatTime()}",
  )
}
