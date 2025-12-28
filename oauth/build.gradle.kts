plugins {
  id("ozone-dokka")
  id("ozone-multiplatform")
  id("ozone-publish")
  kotlin("plugin.serialization")
}

ozone {
  js()
  jvm()
  ios("BlueskyAPIOAuth")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":api-gen-runtime"))

        api(libs.ktor.core)

        implementation(libs.crypto.core)
        implementation(libs.crypto.random)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.contentnegotiation)
        implementation(libs.ktor.serialization.json)
      }
    }
    val iosMain by getting {
      dependencies {
        implementation(libs.crypto.apple)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.crypto.jdk)
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(libs.crypto.webcrypto)
      }
    }
  }
}
