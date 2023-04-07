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

kotlin {
  android()
  jvm("desktop")

  sourceSets {
    val commonMain by getting {
      dependencies {
        api("io.github.xxfast:kstore:0.5.0")
        api("io.github.xxfast:kstore-file:0.5.0")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
        implementation(kotlin("reflect"))
      }
    }
    val desktopMain by getting {
      dependencies {
        implementation("ca.gosyer:kotlin-multiplatform-appdirs:1.0.0")
      }
    }
  }
}
