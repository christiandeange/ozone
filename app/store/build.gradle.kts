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
        api(libs.kotlinx.coroutines)

        implementation(libs.kotlinx.serialization.json)
        implementation(libs.multiplatform.settings)
        implementation(libs.multiplatform.settings.serialization)
        implementation(kotlin("reflect"))
      }
    }
  }
}
