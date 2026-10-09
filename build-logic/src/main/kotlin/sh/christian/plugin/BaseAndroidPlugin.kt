package sh.christian.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName

@Suppress("unused")
class BaseAndroidPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  plugins.apply("ozone-base")

  when (val android = extensions.getByName("android")) {
    is ApplicationExtension -> android.configureOzoneAndroid()
    is LibraryExtension -> android.configureOzoneAndroid()
    else -> error("Unsupported Android extension: ${android::class.qualifiedName}")
  }
}

private fun ApplicationExtension.configureOzoneAndroid() {
  compileSdk = 37

  defaultConfig {
    minSdk = 30
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

private fun LibraryExtension.configureOzoneAndroid() {
  compileSdk = 37

  defaultConfig {
    minSdk = 30
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}
