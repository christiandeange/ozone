package sh.christian.plugin

import com.android.build.api.dsl.CommonExtension
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

  extensions.getByName<CommonExtension<*, *, *, *, *, *>>("android").apply {
    compileSdk = 35

    defaultConfig {
      minSdk = 30
    }

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
    }
  }
}
