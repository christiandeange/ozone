import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.sources.android.findAndroidSourceSet

plugins {
  kotlin("plugin.serialization")
  id("ozone-multiplatform")
  id("ozone-compose")
  id("com.google.devtools.ksp")
}

ozone {
  androidLibrary {
    namespace = "sh.christian.ozone.common"
  }
  js()
  jvm()
  ios()
}

kotlin {
  @Suppress("OPT_IN_USAGE")
  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }

  sourceSets {
    val commonMain by getting {
      kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

      dependencies {
        api(compose.foundation)
        api(compose.material3)
        api(compose.runtime)

        api(libs.kotlinx.serialization.core)
        api(libs.workflow.core)
        api(libs.workflow.runtime)

        // Uncomment to fetch all icons.
        // implementation(libs.androidx.icons.extended)
        implementation(libs.codepoints.deluxe)
        implementation(compose.components.resources)
        implementation(libs.kamel)
        implementation(libs.kotlininject)
        implementation(libs.kotlinx.atomicfu)
        implementation(libs.kotlinx.coroutines)
        implementation(libs.ktor.logging)

        api(project(":bluesky"))
        api(project(":app:store"))

        runtimeOnly(libs.slf4j.simple)
      }
    }
    val androidMain by getting {
      dependencies {
        implementation(libs.androidx.activity.compose)
        implementation(libs.ktor.cio)
        implementation(libs.zoomable)
      }
    }
    val iosMain by getting {
      dependencies {
        implementation(libs.ktor.darwin)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.apache.commons)
        implementation(libs.ktor.cio)
        implementation(libs.zoomable)
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(libs.ktor.js)
      }
    }
  }
}

dependencies {
  add("kspCommonMainMetadata", libs.kotlininject.compiler)
}

tasks.withType<KotlinCompile<*>>().configureEach {
  if (name != "kspCommonMainKotlinMetadata") {
    dependsOn("kspCommonMainKotlinMetadata")
  }
}
