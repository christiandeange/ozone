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

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.serialization.json)
        implementation(kotlin("reflect"))
      }
    }
    val androidMain by getting {
      dependencies {
        implementation(libs.appdirs)
        implementation(libs.kstore.file)
      }
    }
    val desktopMain by getting {
      dependencies {
        implementation(libs.appdirs)
        implementation(libs.kstore.file)
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(libs.kstore.storage)
      }
    }
  }
}
