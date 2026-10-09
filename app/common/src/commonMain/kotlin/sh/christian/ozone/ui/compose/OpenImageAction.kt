package sh.christian.ozone.ui.compose

import kotlinx.collections.immutable.persistentListOf
import sh.christian.ozone.util.ReadOnlyList

/** A renderer-neutral request to open one image from an image collection. */
data class OpenImageAction(
  val images: ReadOnlyList<BasicImage>,
  val selectedIndex: Int,
) {
  init {
    check(images.isNotEmpty()) { "List of images is empty" }
  }

  constructor(image: BasicImage) : this(persistentListOf(image), 0)
}

data class BasicImage(
  val imageUrl: String,
  val alt: String?,
)
