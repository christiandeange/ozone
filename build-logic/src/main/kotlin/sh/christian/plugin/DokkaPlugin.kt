package sh.christian.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.dokka.gradle.DokkaExtension

@Suppress("unused")
class DokkaPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  plugins.apply("org.jetbrains.dokka")

  val libs = project.rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")

  extensions.configure<DokkaExtension> {
    dokkaSourceSets.configureEach {
      reportUndocumented.set(true)
      suppressGeneratedFiles.set(false)

      externalDocumentationLinks.register("kotlinx.serialization") {
        url("https://kotlinlang.org/api/kotlinx.serialization/")
      }

      val ktorVersion = libs.findVersion("ktor").get().displayName.replaceAfterLast('.', "x")
      externalDocumentationLinks.register("ktor-client-core") {
        url("https://api.ktor.io/$ktorVersion")
        packageListUrl("https://api.ktor.io/$ktorVersion/package-list")
      }
    }
  }

  rootProject.dependencies {
    add("dokka", project(path))
  }
}
