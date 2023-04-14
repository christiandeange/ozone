package sh.christian.ozone.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import sh.christian.ozone.home.SelectedHomeScreenTab.NOTIFICATIONS
import sh.christian.ozone.home.SelectedHomeScreenTab.SETTINGS
import sh.christian.ozone.home.SelectedHomeScreenTab.TIMELINE
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.compose.rememberSystemInsets
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen

class HomeScreen(
  private val homeContent: List<ViewRendering>,
  private val unreadCount: String?,
  private val tab: SelectedHomeScreenTab,
  private val onChangeTab: (SelectedHomeScreenTab) -> Unit,
  private val onExit: () -> Unit,
) : ViewRendering by screen({
  Surface(modifier = Modifier.onBackPressed(onExit)) {
    Scaffold(
      modifier = Modifier.padding(rememberSystemInsets()),
      contentWindowInsets = WindowInsets(0.dp),
      bottomBar = {
        NavigationBar(windowInsets = WindowInsets(0.dp)) {
          NavigationBarItem(
            selected = tab == TIMELINE,
            onClick = { onChangeTab(TIMELINE) },
            icon = { Icon(rememberVectorPainter(Icons.Default.Home), "Home") },
            label = { Text("Home") },
          )
          NavigationBarItem(
            selected = tab == NOTIFICATIONS,
            onClick = { onChangeTab(NOTIFICATIONS) },
            icon = {
              BadgedBox(
                badge = {
                  if (unreadCount != null) {
                    Badge { Text(unreadCount) }
                  }
                },
                content = {
                  Icon(rememberVectorPainter(Icons.Default.Notifications), "Dings")
                },
              )
            },
            label = { Text("Dings") },
          )
          NavigationBarItem(
            selected = tab == SETTINGS,
            onClick = { onChangeTab(SETTINGS) },
            icon = { Icon(rememberVectorPainter(Icons.Default.Settings), "Settings") },
            label = { Text("Settings") },
          )
        }
      },
    ) { contentPadding ->
      Box(Modifier.padding(contentPadding)) {
        homeContent.forEach { it.Content() }
      }
    }
  }
})

enum class SelectedHomeScreenTab {
  TIMELINE,
  NOTIFICATIONS,
  SETTINGS,
}
