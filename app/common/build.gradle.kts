import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
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
}

kotlin {
  @Suppress("OPT_IN_USAGE")
  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(compose.foundation)
        api(compose.material3)
        api(compose.runtime)

        api(libs.kotlinx.serialization.core)
        api(libs.workflow.core)
        api(libs.workflow.runtime)

        // Uncomment to fetch all icons.
        // implementation(libs.androidx.icons.extended)
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
      @OptIn(ExperimentalKotlinGradlePluginApi::class)
      findAndroidSourceSet(this)!!.assets.srcDir("fonts")

      dependencies {
        implementation(libs.androidx.activity.compose)
        implementation(libs.ktor.cio)
        implementation(libs.zoomable)
      }
    }
    val jvmMain by getting {
      kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
      resources.srcDir("fonts")

      dependencies {
        implementation(libs.apache.commons)
        implementation(libs.ktor.cio)
        implementation(libs.zoomable)
      }
    }
    val jsMain by getting {
      kotlin.srcDir("build/generated/ksp/js/jsMain/kotlin")
      resources.srcDir("fonts")

      dependencies {
        implementation(libs.ktor.js)
      }
    }
  }
}

dependencies {
  add("kspAndroid", libs.kotlininject.compiler)
  add("kspJs", libs.kotlininject.compiler)
  add("kspJvm", libs.kotlininject.compiler)
}
