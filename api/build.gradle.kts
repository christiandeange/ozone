plugins {
  id("com.android.library")
  id("sh.christian.ozone.api-gen")
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

android {
  namespace = "sh.christian.ozone.api"
  compileSdk = 33

  defaultConfig {
    minSdk = 30
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

kotlin {
  android()
  jvm("desktop")

  sourceSets {
    val commonMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

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
