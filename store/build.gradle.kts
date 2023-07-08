plugins {
  id("ozone-multiplatform")
  kotlin("plugin.serialization")
}

android {
  namespace = "sh.christian.ozone.store"
}

kotlin {
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
