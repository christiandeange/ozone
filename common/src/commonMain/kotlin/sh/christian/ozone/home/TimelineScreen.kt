package sh.christian.ozone.home

import androidx.compose.material3.Text
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen

class TimelineScreen(
  private val timeline: String,
  private val onSignOut: () -> Unit,
  private val onExit: () -> Unit,
) : ViewRendering by screen({
  Text(timeline)
})
