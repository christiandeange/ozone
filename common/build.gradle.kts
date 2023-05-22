import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.sources.android.findAndroidSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("org.jetbrains.compose")
  id("com.android.library")
  id("com.google.devtools.ksp") version "1.8.20-1.0.11"
}

kotlin {
  android()
  jvm("desktop") {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }

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

        implementation("me.tatarka.inject:kotlin-inject-runtime:0.6.1")
        implementation("org.jetbrains.kotlinx:atomicfu:0.20.2")

        api(project(":api"))
        api(project(":store"))
      }
    }
    val androidMain by getting {
      @OptIn(ExperimentalKotlinGradlePluginApi::class)
      findAndroidSourceSet(this)!!.assets.srcDir("fonts")

      dependencies {
        implementation("androidx.activity:activity-compose:1.7.1")

        implementation("androidx.credentials:credentials:1.2.0-alpha04")
        implementation("androidx.credentials:credentials-play-services-auth:1.2.0-alpha04")
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
  ksp("me.tatarka.inject:kotlin-inject-compiler-ksp:0.6.1")
}

android {
  compileSdkPreview = "UpsideDownCake"
  namespace = "sh.christian.ozone.common"

  defaultConfig {
    minSdk = 30
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
tasks.withType<KotlinCompile>().configureEach { kotlinOptions.jvmTarget = "11" }
