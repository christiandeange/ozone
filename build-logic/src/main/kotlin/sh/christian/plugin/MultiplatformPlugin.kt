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

  val kotlin = project.kotlinExtension as KotlinMultiplatformExtension
  kotlin.applyDefaultHierarchyTemplate()

  extensions.create<OzoneExtension>("ozone", this)

  with(kotlin.sourceSets) {
    named("commonTest").configure {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}
