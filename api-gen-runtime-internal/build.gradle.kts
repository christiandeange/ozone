plugins {
  id("ozone-multiplatform")
  id("ozone-publish")
  kotlin("plugin.serialization")
}

ozone {
  js()
  jvm()
  ios()
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.coroutines)
        api(libs.kotlinx.serialization.core)
        api(libs.ktor.core)

        api(project(":api-gen-runtime"))

        implementation(libs.kotlinx.serialization.cbor)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.contentnegotiation)
        implementation(libs.ktor.serialization.json)
        implementation(libs.ktor.websockets)

        implementation(kotlin("reflect"))
      }
    }
  }
}
