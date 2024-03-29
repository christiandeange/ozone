plugins {
  id("ozone-dokka")
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
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.immutable)
        api(libs.kotlinx.serialization.cbor)
        api(libs.kotlinx.serialization.json)

        implementation(kotlin("reflect"))
      }
    }
  }
}
