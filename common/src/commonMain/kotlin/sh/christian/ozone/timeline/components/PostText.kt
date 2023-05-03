package sh.christian.ozone.timeline.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import sh.christian.ozone.model.LinkTarget
import sh.christian.ozone.model.TimelinePost
import sh.christian.ozone.model.TimelinePostFeature.ExternalFeature
import sh.christian.ozone.model.TimelinePostLink
import sh.christian.ozone.user.UserReference
import sh.christian.ozone.util.byteOffsets

@OptIn(ExperimentalTextApi::class)
@Composable
internal fun PostText(
  post: TimelinePost,
  onClick: () -> Unit,
  onOpenUser: (UserReference) -> Unit,
) {
  val maybeExternalLink = (post.feature as? ExternalFeature)?.uri
  val text = post.text.removeSuffix(maybeExternalLink.orEmpty()).trim()

  if (text.isNotBlank()) {
    val postText = rememberFormattedTextPost(text, post.textLinks)

    val uriHandler = LocalUriHandler.current
    ClickableText(
      text = postText,
      style = LocalTextStyle.current.copy(color = LocalContentColor.current),
      onClick = { index ->
        var performedAction = false
        postText.getStringAnnotations("did", index, index).firstOrNull()?.item?.let { did ->
          performedAction = true
          onOpenUser(UserReference.Did(did))
        }
        postText.getUrlAnnotations(index, index).firstOrNull()?.item?.url?.let { url ->
          performedAction = true
          uriHandler.openUri(url)
        }
        if (!performedAction) {
          onClick()
        }
      },
    )
  }
}

@Composable
fun rememberFormattedTextPost(
  text: String,
  textLinks: List<TimelinePostLink>,
): AnnotatedString {
  return remember(text, textLinks) { formatTextPost(text, textLinks) }
}

@OptIn(ExperimentalTextApi::class)
fun formatTextPost(
  text: String,
  textLinks: List<TimelinePostLink>,
): AnnotatedString {
  return buildAnnotatedString {
    append(text)

    val byteOffsets = text.byteOffsets()
    textLinks.forEach { link ->
      if (link.start < byteOffsets.size && link.end < byteOffsets.size) {
        val start = byteOffsets[link.start]
        val end = byteOffsets[link.end]

        addStyle(
          style = SpanStyle(color = Color(0xFF3B62FF)),
          start = start,
          end = end,
        )

        when (link.target) {
          is LinkTarget.ExternalLink -> {
            addUrlAnnotation(UrlAnnotation(link.target.url), start, end)
          }
          is LinkTarget.UserMention -> {
            addStringAnnotation("did", link.target.did, start, end)
          }
        }
      }
    }
  }
}
