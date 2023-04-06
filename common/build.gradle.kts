import org.jetbrains.kotlin.gradle.plugin.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.sources.android.findAndroidSourceSet

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("org.jetbrains.compose")
  id("com.android.library")
}

kotlin {
  android()
  jvm("desktop")

  sourceSets {
    all {
      languageSettings.apply {
        optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn("androidx.compose.ui.ExperimentalComposeUiApi")
      }
    }

    val commonMain by getting {
      dependencies {
        api(compose.runtime)
        api(compose.foundation)
        api(compose.material3)

        api("com.squareup.workflow1:workflow-core:1.10.0-beta01")
        api("com.squareup.workflow1:workflow-runtime:1.10.0-beta01")

        // Uncomment to fetch all icons.
        // implementation("androidx.compose.material:material-icons-extended:1.3.1")
        implementation("com.alialbaali.kamel:kamel-image:0.4.1")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

        api(project(":api"))
        api(project(":store"))
      }
    }
    val androidMain by getting {
      @OptIn(ExperimentalKotlinGradlePluginApi::class)
      findAndroidSourceSet(this)!!.assets.srcDir("fonts")

      dependencies {
        implementation("androidx.activity:activity-compose:1.6.1")
      }
    }
    val desktopMain by getting {
      sourceSets {
        resources.srcDir("fonts")
      }

      dependencies {
        implementation("org.apache.commons:commons-lang3:3.12.0")
      }
    }
  }
}

android {
  compileSdk = 33
  namespace = "sh.christian.ozone.common"

  defaultConfig {
    minSdk = 30
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}
