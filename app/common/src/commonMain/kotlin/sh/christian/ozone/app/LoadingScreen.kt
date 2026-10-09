package sh.christian.ozone.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import sh.christian.ozone.ui.renderer.ScreenModel

class LoadingScreen(
  private val message: String,
) : ScreenModel {
  @Composable
  override fun Content() {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      CircularProgressIndicator()
      Text(message)
    }
  }
}
