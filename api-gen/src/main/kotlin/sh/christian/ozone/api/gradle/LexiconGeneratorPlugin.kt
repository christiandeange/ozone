package sh.christian.ozone.api.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import sh.christian.ozone.buildconfig.Dependencies

class LexiconGeneratorPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  val extension = extensions.create<LexiconGeneratorExtension>("lexicons")
  val configuration = configurations.create("lexicons")

  val generateLexicons = tasks.register<LexiconGeneratorTask>("generateLexicons") {
    schemasClasspath.from(configuration)
    apiName.set(extension.apiName)
    outputDirectory.set(extension.outputDirectory)
  }

  val pluginConfiguration: Plugin<*>.() -> Unit = {
    tasks.locateTask<Jar>("sourcesJar")?.configure {
      dependsOn(generateLexicons)
    }

    val generatedSrcDir = extension.outputDirectory

    when (val kotlinExtension = project.kotlinExtension) {
      is KotlinSingleTargetExtension<*> -> {
        kotlinExtension.target.apply {
          applyConfiguration("main", generatedSrcDir, generateLexicons)
        }
      }

      is KotlinMultiplatformExtension -> {
        kotlinExtension.targets.configureEach {
          applyConfiguration("commonMain", generatedSrcDir, generateLexicons)
        }
      }

      else -> error("Unknown Kotlin plugin extension: $kotlinExtension")
    }
  }

  plugins.withId("org.jetbrains.kotlin.android", pluginConfiguration)
  plugins.withId("org.jetbrains.kotlin.jvm", pluginConfiguration)
  plugins.withId("org.jetbrains.kotlin.multiplatform", pluginConfiguration)
}

private fun KotlinTarget.applyConfiguration(
  sourceSetName: String,
  generatedSrcDir: DirectoryProperty,
  compileTaskDependency: TaskProvider<*>,
) {
  project.plugins.apply("org.jetbrains.kotlin.plugin.serialization")
  project.kotlinExtension.sourceSets.getByName(sourceSetName).apply {
    kotlin.srcDir(generatedSrcDir)
    dependencies {
      api(Dependencies.KOTLINX_DATETIME)
      api(Dependencies.KTOR_CORE)

      // Expose certain types that are publicly used in the generated classes.
      api(project(":api-gen-runtime-api"))

      // Keep some internal utility methods only on the runtime classpath
      implementation(project(":api-gen-runtime-implementation"))
    }
  }

  components.forEach {
    project.tasks.locateTask<Jar>("${it.name}SourcesJar")?.configure {
      dependsOn(compileTaskDependency)
    }
  }

  compilations.configureEach {
    compileTaskProvider.configure {
      dependsOn(compileTaskDependency)
    }
  }
}

private inline fun <reified T : Task> TaskContainer.locateTask(name: String): TaskProvider<T>? {
  return try {
    withType(T::class.java).named(name)
  } catch (e: UnknownTaskException) {
    null
  }
}
