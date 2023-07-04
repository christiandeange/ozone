package sh.christian.ozone.compose

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import sh.christian.ozone.api.Did
import sh.christian.ozone.api.Handle
import sh.christian.ozone.api.Uri
import sh.christian.ozone.model.LinkTarget
import sh.christian.ozone.model.Profile
import sh.christian.ozone.model.TimelinePostLink
import sh.christian.ozone.timeline.components.PostReplyLine
import sh.christian.ozone.timeline.components.formatTextPost
import sh.christian.ozone.ui.compose.AvatarImage
import sh.christian.ozone.ui.compose.onBackPressed
import sh.christian.ozone.ui.compose.rememberSystemInsets
import sh.christian.ozone.ui.workflow.ViewRendering
import sh.christian.ozone.ui.workflow.screen
import sh.christian.ozone.util.byteOffsets
import sh.christian.ozone.util.color
import kotlin.math.min
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.unit.lerp as lerpDp

class ComposePostScreen(
  private val profile: Profile,
  private val replyingTo: Profile?,
  private val onExit: () -> Unit,
  private val onPost: (PostPayload) -> Unit,
) : ViewRendering by screen({
  val postTextLimit = 300
  var postText by remember { mutableStateOf(TextFieldValue(AnnotatedString(""))) }
  val postPayload by lazy {
    PostPayload(
      authorDid = profile.did,
      text = postText.text,
      links = postText.annotatedString.links(),
    )
  }

  Surface(Modifier.onBackPressed(onExit)) {
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .padding(rememberSystemInsets()),
      contentWindowInsets = WindowInsets(0.dp),
      topBar = {
        TopAppBar(
          windowInsets = WindowInsets(0.dp),
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
              enabled = postText.annotatedString.isNotEmpty(),
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
          contentDescription = profile.displayName ?: profile.handle.handle,
          fallbackColor = profile.handle.color(),
        )

        Column {
          PostReplyLine(
            modifier = Modifier.padding(bottom = 8.dp),
            replyingTo = replyingTo,
            onOpenUser = { },
          )

          val textFieldFocusRequester = remember { FocusRequester() }

          BasicTextField(
            modifier = Modifier
              .fillMaxSize()
              .weight(1f)
              .focusRequester(textFieldFocusRequester),
            value = postText,
            onValueChange = {
              postText = it.copy(
                annotatedString = formatTextPost(it.text, it.annotatedString.links())
              )
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
            keyboardOptions = KeyboardOptions(
              imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions {
              if (postText.annotatedString.isNotEmpty()) {
                onPost(postPayload)
              }
            },
          )

          LaunchedEffect(Unit) {
            textFieldFocusRequester.requestFocus()
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 16.dp),
            horizontalArrangement = spacedBy(16.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            val postByteCount = postText.text.codePointCount(0, postText.text.length)
            val unboundedProgress = postByteCount / postTextLimit.toFloat()

            Text(
              modifier = Modifier.padding(top = 12.dp),
              textAlign = TextAlign.Right,
              text = (postTextLimit - postByteCount).toString(),
            )

            val progress = min(1f, unboundedProgress)
            val easing = remember { CubicBezierEasing(.42f, 0f, 1f, 0.58f) }

            CircularProgressIndicator(
              modifier = Modifier.height(24.dp),
              progress = progress,
              strokeWidth = lerpDp(
                start = 8.dp,
                stop = 24.dp,
                fraction = ((unboundedProgress - 1) * 4).coerceIn(0f, 1f),
              ),
              color = lerpColor(
                start = MaterialTheme.colorScheme.primary,
                stop = Color.Red,
                fraction = easing.transform(progress),
              )
            )
          }
        }
      }
    }
  }
})

private fun AnnotatedString.links(): ImmutableList<TimelinePostLink> {
  val byteOffsets = text.byteOffsets()
  val handleRegex = Regex(
    "(^|\\s|\\()(@)([a-zA-Z0-9.-]+)(\\b)",
  )
  val hyperlinkRegex = Regex(
    "(^|\\s|\\()((https?://\\S+)|(([a-z][a-z0-9]*(\\.[a-z0-9]+)+)\\S*))",
    setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE),
  )

  val mentions = handleRegex.findAll(text)
    .map {
      TimelinePostLink(
        start = byteOffsets.indexOf(it.range.first),
        end = byteOffsets.indexOf(it.range.last + 1),
        // Ok this is actually a handle for now, but it is resolved to a Did later on.
        target = LinkTarget.UserHandleMention(Handle(it.groupValues[3])),
      )
    }

  val hyperlinks = hyperlinkRegex.findAll(text)
    .map {
      var url = it.groupValues[2]
      if (!url.startsWith("http")) {
        url = "https://$url"
      }
      url = url.dropLastWhile { c -> c in ".,;!?" }
      if (url.endsWith(')') && '(' !in url) {
        url = url.dropLast(1)
      }

      TimelinePostLink(
        start = byteOffsets.indexOf(it.range.first),
        end = byteOffsets.indexOf(it.range.last + 1),
        target = LinkTarget.ExternalLink(Uri(url)),
      )
    }

  return (mentions + hyperlinks).toImmutableList()
}

data class PostPayload(
  val authorDid: Did,
  val text: String,
  val links: ImmutableList<TimelinePostLink>,
)
