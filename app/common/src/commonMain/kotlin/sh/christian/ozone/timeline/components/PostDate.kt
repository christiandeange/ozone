package sh.christian.ozone.timeline.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import sh.christian.ozone.model.Moment
import sh.christian.ozone.util.formatDate
import sh.christian.ozone.util.formatTime

@Composable
fun PostDate(time: Moment) {
  Text(
    text = "${time.formatDate()} • ${time.formatTime()}",
  )
}
