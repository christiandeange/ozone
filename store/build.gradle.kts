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
        api(libs.kstore)
        api(libs.kstore.file)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.serialization.json)
        implementation(kotlin("reflect"))
      }
    }
    val desktopMain by getting {
      dependencies {
        implementation(libs.appdirs)
      }
    }
  }
}
