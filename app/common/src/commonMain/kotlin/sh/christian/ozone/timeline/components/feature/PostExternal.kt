package sh.christian.ozone.timeline.components.feature

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import sh.christian.ozone.api.Uri
import sh.christian.ozone.model.TimelinePostFeature.ExternalFeature
import sh.christian.ozone.ui.compose.PostImage

@Composable
fun PostExternal(
  feature: ExternalFeature,
  onClick: () -> Unit,
) {
  val uriHandler = LocalUriHandler.current

  FeatureContainer(onClick = onClick) {
    Row(horizontalArrangement = spacedBy(16.dp)) {
      if (!feature.thumb.isNullOrBlank()) {
        PostImage(
          modifier = Modifier.requiredSizeIn(maxWidth = 96.dp, maxHeight = 96.dp),
          imageUrl = feature.thumb,
          contentDescription = feature.title,
          onClick = { uriHandler.openUri(feature.uri.uri) },
          fallbackColor = MaterialTheme.colorScheme.outline,
        )
      }
      PostFeatureTextContent(
        modifier = Modifier.weight(1f),
        title = feature.title,
        description = feature.description,
        uri = feature.uri,
      )
    }
  }
}

@Composable
fun PostFeatureTextContent(
  modifier: Modifier = Modifier,
  title: String?,
  description: String?,
  uri: Uri?,
) {
  Column(modifier, verticalArrangement = spacedBy(4.dp)) {
    if (!title.isNullOrBlank()) {
      Text(
        text = title,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = Bold),
      )
    }
    if (!description.isNullOrBlank()) {
      Text(
        text = description,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge,
      )
    }
    val url = uri?.uri
    if (!url.isNullOrBlank()) {
      Text(
        text = url,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}
