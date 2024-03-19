package sh.christian.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask

@Suppress("unused")
class DokkaPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  plugins.apply("org.jetbrains.dokka")

  val libs = project.rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")

  tasks.withType<AbstractDokkaLeafTask>().configureEach {
    dokkaSourceSets.configureEach {
      reportUndocumented.set(true)
      suppressGeneratedFiles.set(false)

      externalDocumentationLink(
        url = "https://kotlinlang.org/api/kotlinx.serialization/",
      )

      val ktorVersion = libs.findVersion("ktor").get().displayName
      externalDocumentationLink(
        url = "https://api.ktor.io/older/$ktorVersion/ktor-client",
        packageListUrl = "https://api.ktor.io/older/$ktorVersion/package-list",
      )
    }
  }
}
