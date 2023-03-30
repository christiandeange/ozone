package sh.christian.ozone.compose

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import sh.christian.ozone.model.Profile
import sh.christian.ozone.ui.compose.AvatarImage
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen
import sh.christian.ozone.util.color

@OptIn(ExperimentalMaterial3Api::class)
class ComposePostScreen(
  private val profile: Profile,
  private val onExit: () -> Unit,
  private val onPost: (PostPayload) -> Unit,
) : ViewRendering by screen({
  var postText by remember { mutableStateOf("") }
  val postPayload by derivedStateOf {
    PostPayload(
      authorDid = profile.did,
      text = postText,
    )
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .onBackPressed(onExit),
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onExit) {
            Icon(
              painter = rememberVectorPainter(Icons.Default.Close),
              contentDescription = "Cancel",
            )
          }
        },
        title = {},
        actions = {
          OutlinedButton(
            modifier = Modifier.padding(end = 8.dp),
            enabled = postText.isNotEmpty(),
            onClick = { onPost(postPayload) },
          ) {
            Text("Post")
          }
        },
      )
    },
  ) { contentPadding ->
    Row(
      modifier = Modifier.padding(contentPadding).padding(16.dp),
      horizontalArrangement = spacedBy(16.dp),
    ) {
      AvatarImage(
        modifier = Modifier.size(48.dp),
        avatarUrl = profile.avatar,
        contentDescription = profile.displayName ?: profile.handle,
        fallbackColor = profile.handle.color(),
      )

      val textFieldFocusRequester = remember { FocusRequester() }

      BasicTextField(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 8.dp)
          .focusRequester(textFieldFocusRequester),
        value = postText,
        onValueChange = { postText = it },
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = TextStyle(color = LocalContentColor.current),
        keyboardOptions = KeyboardOptions(
          imeAction = ImeAction.Send,
        ),
        keyboardActions = KeyboardActions {
          if (postText.isNotEmpty()) {
            onPost(postPayload)
          }
        },
      )

      LaunchedEffect(Unit) {
        textFieldFocusRequester.requestFocus()
      }
    }
  }
})

data class PostPayload(
  val authorDid: String,
  val text: String,
)
