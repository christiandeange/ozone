package sh.christian.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("unused")
class ComposePlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  plugins.apply("org.jetbrains.compose")
  plugins.apply("org.jetbrains.kotlin.plugin.compose")

  project.kotlinExtension.apply {
    sourceSets.all {
      languageSettings.apply {
        optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn("androidx.compose.ui.ExperimentalComposeUiApi")
      }
    }
  }

  tasks.withType<KotlinCompile>().configureEach {
    if (project.findProperty("enableComposeCompilerReports") == "true") {
      val destinationPath = project.buildDir.absolutePath + "/compose_metrics"
      compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.set(
          freeCompilerArgs.get() + listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$destinationPath",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$destinationPath"
          ),
        )
      }
    }
  }
}
