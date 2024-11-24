plugins {
  id("ozone-dokka")
  id("ozone-multiplatform")
  id("ozone-publish")
  kotlin("plugin.serialization")
}

ozone {
  js()
  jvm()
  ios("BlueskyAPIRuntime")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.serialization.cbor)
        api(libs.kotlinx.serialization.json)
        api(libs.ktor.core)

        implementation(kotlin("reflect"))
      }
    }
    val iosMain by getting {
      dependencies {
        api(libs.ktor.darwin)
      }
    }
  }
}
