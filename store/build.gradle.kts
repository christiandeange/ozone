import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.library")
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

android {
  namespace = "sh.christian.ozone.store"
  compileSdk = 33

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

kotlin {
  android()
  jvm("desktop") {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api("io.github.xxfast:kstore:0.6.0")
        api("io.github.xxfast:kstore-file:0.6.0")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
        implementation(kotlin("reflect"))
      }
    }
    val desktopMain by getting {
      dependencies {
        implementation("ca.gosyer:kotlin-multiplatform-appdirs:1.1.0")
      }
    }
  }
}
