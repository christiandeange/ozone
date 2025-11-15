package sh.christian.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused")
class MultiplatformPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  extensions.create<OzoneExtension>("ozone", this)
  extensions.findByType<KotlinMultiplatformExtension>()?.apply {
      compilerOptions.apply {
          optIn.add("kotlin.time.ExperimentalTime")
      }
  }
}
