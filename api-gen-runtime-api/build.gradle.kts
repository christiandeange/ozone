import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.library")
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

android {
  namespace = "sh.christian.ozone.api.runtime.api"
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
        api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

        implementation(kotlin("reflect"))
      }
    }
  }

  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of("11"))
  }
}
