package sh.christian.ozone.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import sh.christian.ozone.model.Notification
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen

@OptIn(ExperimentalFoundationApi::class)
class NotificationsScreen(
  private val now: Instant,
  private val notifications: List<Notification>,
  private val onLoadMore: () -> Unit,
  private val onExit: () -> Unit,
) : ViewRendering by screen({
  Surface(modifier = Modifier.onBackPressed(onExit)) {
    Scaffold(
      contentWindowInsets = WindowInsets(0.dp),
      topBar = {
        CenterAlignedTopAppBar(
          windowInsets = WindowInsets(0.dp),
          title = {
            Text(
              text = "Notifications",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
          },
        )
      },
    ) { contentPadding ->
      LazyColumn(Modifier.padding(contentPadding).fillMaxSize()) {
        stickyHeader {
          Divider(thickness = Dp.Hairline)
        }

        items(notifications) {
          Column {
            Column(Modifier.padding(16.dp), verticalArrangement = spacedBy(4.dp)) {
              // TODO actually format notifications.
              Text(it.reason)
            }
            Divider(thickness = Dp.Hairline)
          }
        }
      }
    }
  }
})
