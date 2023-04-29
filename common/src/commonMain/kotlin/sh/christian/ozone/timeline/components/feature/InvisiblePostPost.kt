package sh.christian.ozone.timeline.components.feature

import androidx.compose.runtime.Composable

@Composable
fun InvisiblePostPost(onClick: (() -> Unit)?) {
  FeatureContainer(onClick = onClick) {
    PostFeatureTextContent(
      title = "Cannot be loaded.",
      description = "This post is unable to be viewed.",
      url = null,
    )
  }
}
