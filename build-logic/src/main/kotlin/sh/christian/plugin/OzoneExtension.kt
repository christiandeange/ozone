package sh.christian.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

abstract class OzoneExtension(
  private val project: Project,
) {
  fun androidLibrary(configure: KotlinMultiplatformAndroidLibraryTarget.() -> Unit = {}) {
    project.plugins.apply("com.android.kotlin.multiplatform.library")
    project.plugins.apply("ozone-android")
    val android = (project.kotlinExtension as ExtensionAware)
      .extensions
      .getByName("android") as KotlinMultiplatformAndroidLibraryTarget
    android.configure()
  }

  fun androidApp(configure: ApplicationExtension.() -> Unit = {}) {
    check(project.plugins.hasPlugin("ozone-android-app")) {
      "ozone-android-app plugin has not been applied."
    }
    project.extensions.configure(configure)
  }

  @OptIn(ExperimentalWasmDsl::class)
  fun js() {
    kotlinMultiplatform {
      wasmJs {
        browser()
        nodejs()
        binaries.executable()
      }
    }
  }

  fun jvm() {
    kotlinMultiplatform {
      jvm {
        compilations.all {
          compileTaskProvider.configure {
            compilerOptions {
              jvmTarget.set(JvmTarget.JVM_11)
            }
          }
        }
      }
    }
  }

  fun ios(
    name: String,
    configure: Framework.() -> Unit = {},
  ) {
    project.plugins.apply("co.touchlab.skie")

    kotlinMultiplatform {
      listOf(
        iosArm64(),
        iosSimulatorArm64(),
      ).forEach {
        it.binaries.framework {
          baseName = name
          isStatic = true
          configure()
        }
      }
    }
  }

  private fun kotlinMultiplatform(configure: KotlinMultiplatformExtension.() -> Unit = {}) {
    (project.kotlinExtension as KotlinMultiplatformExtension).apply {
      configure()
    }
  }
}
