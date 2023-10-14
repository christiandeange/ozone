package sh.christian.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

@Suppress("unused")
class MultiplatformPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  plugins.apply("com.android.library")
  plugins.apply("org.jetbrains.kotlin.multiplatform")
  plugins.apply("ozone-base")
  plugins.apply("ozone-android")

  (project.kotlinExtension as KotlinMultiplatformExtension).apply {
    androidTarget()

    jvm("desktop") {
      compilations.all {
        kotlinOptions.jvmTarget = "11"
      }
    }
  }
}
