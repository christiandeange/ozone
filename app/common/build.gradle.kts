import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.sources.android.findAndroidSourceSet

plugins {
  kotlin("plugin.serialization")
  id("ozone-multiplatform")
  id("ozone-compose")
  id("com.google.devtools.ksp")
}

android {
  namespace = "sh.christian.ozone.common"
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(compose.foundation)
        api(compose.material3)
        api(compose.runtime)

        api(libs.workflow.core)
        api(libs.workflow.runtime)

        // Uncomment to fetch all icons.
        // implementation(libs.androidx.icons.extended)
        implementation(libs.kamel)
        implementation(libs.kotlininject)
        implementation(libs.kotlinx.atomicfu)
        implementation(libs.ktor.cio)
        implementation(libs.ktor.logging)
        implementation(libs.zoomable)

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
      }
    }
    val desktopMain by getting {
      kotlin.srcDir("build/generated/ksp/desktop/desktopMain/kotlin")

      sourceSets {
        resources.srcDir("fonts")
      }

      dependencies {
        implementation(libs.apache.commons)
      }
    }
  }
}

dependencies {
  add("kspAndroid", libs.kotlininject.compiler)
  add("kspDesktop", libs.kotlininject.compiler)
}
