package sh.christian.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

@Suppress("unused")
class MultiplatformPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  extensions.create<OzoneExtension>("ozone", this)
}
