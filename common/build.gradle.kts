import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.sources.android.findAndroidSourceSet

plugins {
  kotlin("plugin.serialization")
  id("ozone-multiplatform")
  id("ozone-compose")
  id("com.google.devtools.ksp") version "1.8.20-1.0.11"
}

android {
  namespace = "sh.christian.ozone.common"
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(compose.runtime)
        api(compose.foundation)
        api(compose.material3)

        api("com.squareup.workflow1:workflow-core:1.11.0-beta01")
        api("com.squareup.workflow1:workflow-runtime:1.11.0-beta01")

        // Uncomment to fetch all icons.
        // implementation("androidx.compose.material:material-icons-extended:1.3.1")
        implementation("media.kamel:kamel-image:0.6.1")

        implementation("me.saket.telephoto:zoomable:0.4.0")
        implementation("me.tatarka.inject:kotlin-inject-runtime:0.6.1")
        implementation("io.ktor:ktor-client-cio:2.3.2")
        implementation("io.ktor:ktor-client-logging:2.3.2")
        implementation("org.jetbrains.kotlinx:atomicfu:0.21.0")
        implementation("org.jetbrains.skiko:skiko:0.7.63")

        api(project(":bluesky"))
        api(project(":store"))

        runtimeOnly("org.slf4j:slf4j-simple:2.0.7")
      }
    }
    val androidMain by getting {
      @OptIn(ExperimentalKotlinGradlePluginApi::class)
      findAndroidSourceSet(this)!!.assets.srcDir("fonts")

      dependencies {
        implementation("androidx.activity:activity-compose:1.7.2")
      }
    }
    val desktopMain by getting {
      kotlin.srcDir("build/generated/ksp/desktop/desktopMain/kotlin")

      sourceSets {
        resources.srcDir("fonts")
      }

      dependencies {
        implementation("org.apache.commons:commons-lang3:3.12.0")
      }
    }
  }
}

dependencies {
  add("kspAndroid", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.6.1")
  add("kspDesktop", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.6.1")
}
