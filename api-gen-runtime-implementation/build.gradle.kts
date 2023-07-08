import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.library")
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

android {
  namespace = "sh.christian.ozone.api.runtime"
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
        api("io.ktor:ktor-client-core:2.3.2")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

        api(project(":api-gen-runtime-api"))

        implementation("io.ktor:ktor-client-content-negotiation:2.3.2")
        implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.2")

        implementation(kotlin("reflect"))
      }
    }
  }

  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of("11"))
  }
}
