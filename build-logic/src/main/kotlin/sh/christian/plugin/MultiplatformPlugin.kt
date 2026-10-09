package sh.christian.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

@Suppress("unused")
class MultiplatformPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  plugins.apply("org.jetbrains.kotlin.multiplatform")
  plugins.apply("ozone-base")

  (project.kotlinExtension as KotlinMultiplatformExtension).applyDefaultHierarchyTemplate()

  extensions.create<OzoneExtension>("ozone", this)
}
