plugins {
  id("ozone-multiplatform")
  kotlin("plugin.serialization")
}

ozone {
  androidLibrary {
    namespace = "sh.christian.ozone.store"
  }
  js()
  jvm()
  ios("OzoneStore")
}

kotlin {
  sourceSets {
    commonMain.configure {
      dependencies {
        api(libs.kotlinx.coroutines.core)

        implementation(libs.kotlinx.serialization.json)
        implementation(libs.multiplatform.settings)
        implementation(libs.multiplatform.settings.serialization)
        implementation(kotlin("reflect"))
      }
    }
  }
}
