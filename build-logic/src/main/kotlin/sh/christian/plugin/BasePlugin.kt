package sh.christian.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("unused")
class BasePlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
  tasks.withType<KotlinCompile>().configureEach { kotlinOptions.jvmTarget = "11" }

  project.kotlinExtension.jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of("11"))
  }
}
