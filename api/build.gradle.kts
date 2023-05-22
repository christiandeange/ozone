import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.library")
  id("sh.christian.ozone.api-gen")
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

android {
  namespace = "sh.christian.ozone.api"
  compileSdkPreview = "UpsideDownCake"

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
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

        api("io.ktor:ktor-client-content-negotiation:2.2.3")
        api("io.ktor:ktor-client-core:2.2.3")
        api("io.ktor:ktor-client-cio:2.2.3")
        api("io.ktor:ktor-client-logging:2.2.3")
        api("io.ktor:ktor-serialization-kotlinx-json:2.2.3")

        implementation(kotlin("reflect"))

        runtimeOnly("org.slf4j:slf4j-simple:2.0.6")
      }
    }
  }
}

dependencies {
  lexicons(fileTree("lexicons") { include("**/*.json") })
}
