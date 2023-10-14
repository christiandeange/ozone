package sh.christian.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask

@Suppress("unused")
class DokkaPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  plugins.apply("org.jetbrains.dokka")

  tasks.withType<AbstractDokkaLeafTask>().configureEach {
    dokkaSourceSets.configureEach {
      reportUndocumented.set(true)
      suppressGeneratedFiles.set(false)
    }
  }
}
