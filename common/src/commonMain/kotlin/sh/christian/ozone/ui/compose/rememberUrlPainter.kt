package sh.christian.ozone.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import io.kamel.core.DataSource
import io.kamel.core.Resource
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.ResourceConfigBuilder
import io.kamel.core.loadImageBitmapResource
import io.kamel.core.loadImageVectorResource
import io.kamel.core.loadSvgResource
import io.kamel.core.map
import io.kamel.image.config.LocalKamelConfig

/**
 * This is a copy of https://github.com/alialbaali/Kamel/pull/33 that can be used instead of
 * `lazyPainterResource` until it's available in a new version.
 *
 * Currently the lazyPainterResource will always result in at least 2 compositions even when the
 * Painter is already cached: the first one always returns Resource.Loading, while the second one
 * is the actual Resource.Success containing the cached Painter.
 *
 * By looking up the cached Painter ahead of time and using that as the initial value if it exists,
 * we skip a recomposition for preloaded Painters and avoid a UI flash in some cases where an
 * external factor causes a recomposition.
 */
@Composable
fun rememberUrlPainter(
  data: Any,
  key: Any? = data,
  filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
): PainterResource {

  val kamelConfig = LocalKamelConfig.current
  val density = LocalDensity.current
  val resourceConfig = remember(key, density) {
    ResourceConfigBuilder()
      .apply { this.density = density }
      .build()
  }

  val cachedOutput = remember(key, resourceConfig) {
    val output = kamelConfig.mapInput(data)
    when (data.toString().substringAfterLast(".")) {
      "svg" -> kamelConfig.svgCache[output]?.let { Resource.Success(it, DataSource.Memory) }
      "xml" -> kamelConfig.imageVectorCache[output]?.let { Resource.Success(it, DataSource.Memory) }
      else -> kamelConfig.imageBitmapCache[output]?.let { Resource.Success(it, DataSource.Memory) }
    } ?: Resource.Loading(0f)
  }

  val painterResource by remember(key, resourceConfig) {
    when (data.toString().substringAfterLast(".")) {
      "svg" -> kamelConfig.loadSvgResource(data, resourceConfig)
      "xml" -> kamelConfig.loadImageVectorResource(data, resourceConfig)
      else -> kamelConfig.loadImageBitmapResource(data, resourceConfig)
    }
  }.collectAsState(cachedOutput, resourceConfig.coroutineContext)

  val kamelResource = painterResource.map { value ->
    when (value) {
      is ImageVector -> rememberVectorPainter(value)
      is ImageBitmap -> remember(value) { BitmapPainter(value, filterQuality = filterQuality) }
      else -> remember(value) { value as Painter }
    }
  }

  return when (kamelResource) {
    is Resource.Failure -> PainterResource.Failure
    is Resource.Loading -> PainterResource.Loading
    is Resource.Success -> PainterResource.Success(kamelResource.value)
  }
}

private fun KamelConfig.mapInput(input: Any): Any {
  var output: Any? = null
  mappers.findLast {
    output = runCatching { it.map(input) }.getOrNull()
    output != null
  }
  return output ?: input
}

@Stable
sealed interface PainterResource {
  @Stable
  object Loading : PainterResource

  @Stable
  object Failure : PainterResource

  @Stable
  data class Success(val painter: Painter) : PainterResource
}
