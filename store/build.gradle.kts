plugins {
  id("com.android.library")
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

android {
  namespace = "sh.christian.ozone.store"
  compileSdk = 33

  defaultConfig {
    minSdk = 24
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
        api("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.5.0")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
      }
    }
    val androidMain by getting {
      dependencies {
        implementation("androidx.datastore:datastore-preferences:1.0.0")
        implementation("androidx.datastore:datastore-preferences-core:1.0.0")
      }
    }
    val desktopMain by getting {
      dependencies {
        implementation("androidx.datastore:datastore-preferences-core:1.0.0")
      }
    }
  }
}
