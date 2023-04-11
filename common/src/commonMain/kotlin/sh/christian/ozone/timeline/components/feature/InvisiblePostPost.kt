package sh.christian.ozone.timeline.components.feature

import androidx.compose.runtime.Composable

@Composable
fun InvisiblePostPost(onClick: (() -> Unit)?) {
  FeatureContainer(onClick = onClick) {
    PostFeatureTextContent(
      title = "This post cannot be loaded.",
      description = "This post is restricted from being viewed.",
      url = null,
    )
  }
}
