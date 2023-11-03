package sh.christian.ozone.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sh.christian.ozone.ui.compose.StablePainter
import sh.christian.ozone.ui.compose.heroFont
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.compose.stable
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen

class SettingsScreen(
  private val onExit: () -> Unit,
  private val onSignOut: () -> Unit,
) : ViewRendering by screen({
  Surface(modifier = Modifier.onBackPressed(onExit)) {
    Scaffold(
      contentWindowInsets = WindowInsets(0.dp),
      topBar = {
        CenterAlignedTopAppBar(
          windowInsets = WindowInsets(0.dp),
          title = {
            Text(
              text = "Settings",
              style = MaterialTheme.typography.titleLarge.copy(fontFamily = heroFont),
            )
          },
        )
      },
    ) { contentPadding ->
      Column(Modifier.padding(contentPadding).fillMaxSize()) {
        SettingsRow(
          icon = rememberVectorPainter(Icons.Default.ExitToApp).stable,
          text = "Sign Out",
          onClick = onSignOut,
        )

        Spacer(Modifier.weight(1f))

        Text(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          text = " Made with ♥️ in Toronto.",
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.bodySmall,
        )
      }
    }
  }
})

@Composable
private fun SettingsRow(
  icon: StablePainter,
  text: String,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(16.dp),
    horizontalArrangement = spacedBy(16.dp),
    verticalAlignment = CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .background(MaterialTheme.colorScheme.outline, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        painter = icon.painter,
        contentDescription = null,
      )
    }

    Text(text)
  }
}
