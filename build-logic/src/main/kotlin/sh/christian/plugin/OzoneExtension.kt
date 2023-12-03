package sh.christian.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

abstract class OzoneExtension(
  private val project: Project,
) {
  init {
    project.plugins.apply("org.jetbrains.kotlin.multiplatform")
    project.plugins.apply("ozone-base")
  }

  fun androidLibrary(configure: LibraryExtension.() -> Unit = {}) {
    project.plugins.apply("com.android.library")
    project.plugins.apply("ozone-android")
    kotlin {
      androidTarget()
    }
    project.extensions.configure(configure)
  }

  fun androidApp(configure: AppExtension.() -> Unit = {}) {
    project.plugins.apply("com.android.application")
    project.plugins.apply("ozone-android")
    kotlin {
      androidTarget()
    }
    project.extensions.configure(configure)
  }

  fun js() {
    kotlin {
      js(IR) {
        browser()
        nodejs()
        binaries.executable()
      }
    }
  }

  fun jvm() {
    kotlin {
      jvm {
        compilations.all {
          kotlinOptions.jvmTarget = "11"
        }
      }
    }
  }

  private fun kotlin(configure: KotlinMultiplatformExtension.() -> Unit) {
    configure(project.kotlinExtension as KotlinMultiplatformExtension)
  }
}
