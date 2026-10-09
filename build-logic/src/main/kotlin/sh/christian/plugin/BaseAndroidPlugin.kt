package sh.christian.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

@Suppress("unused")
class BaseAndroidPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  plugins.apply("ozone-base")

  plugins.withId("com.android.kotlin.multiplatform.library") {
    val android = (kotlinExtension as ExtensionAware)
      .extensions.getByName("android") as KotlinMultiplatformAndroidLibraryTarget
    android.configureOzoneAndroidLibrary()
  }

  plugins.withId("com.android.application") {
    val android = extensions.getByName("android") as ApplicationExtension
    android.configureOzoneAndroidApplication()
  }
}

private fun KotlinMultiplatformAndroidLibraryTarget.configureOzoneAndroidLibrary() {
  compileSdk = 37
  minSdk = 30

  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

private fun ApplicationExtension.configureOzoneAndroidApplication() {
  compileSdk = 37

  defaultConfig {
    minSdk = 30
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}
