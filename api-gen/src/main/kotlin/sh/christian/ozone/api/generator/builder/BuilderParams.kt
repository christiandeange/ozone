package sh.christian.ozone.api.generator.builder

import org.gradle.configurationcache.extensions.capitalized

data class BuilderParams(
  val authority: String,
  val procedureName: String,
  val classPrefix: String = procedureName.capitalized(),
)
