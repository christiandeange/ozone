package sh.christian.ozone.api.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class LexiconGeneratorPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  val extension = extensions.create<LexiconGeneratorExtension>("lexicons")
  val configuration = configurations.create("lexicons")

  val generateLexicons = tasks.register<LexiconGeneratorTask>("generateLexicons") {
    schemasClasspath.from(configuration)
    outputDirectory.set(extension.outputDirectory)
  }

  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    val kotlinExtension = extensions.getByName<KotlinMultiplatformExtension>("kotlin")

    kotlinExtension.sourceSets
      .getByName("commonMain")
      .kotlin
      .srcDir(extension.outputDirectory)

    kotlinExtension.targets.configureEach {
      compilations.configureEach {
        compileTaskProvider
          .configure {
            dependsOn(generateLexicons)
          }
      }
    }
  }
}
