plugins {
  id("ozone-dokka")
  id("ozone-multiplatform")
  id("ozone-publish")
  id("org.jetbrains.kotlinx.binary-compatibility-validator")
  kotlin("plugin.serialization")
}

ozone {
  js()
  jvm()
  ios("OzoneOAuth")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("sh.christian.ozone:api-gen-runtime-internal:$version")
        implementation(libs.crypto.random)
        implementation(libs.kotlinx.serialization.json)

        api(libs.crypto.core)
        api(libs.kotlinx.datetime)
        api(libs.ktor.core)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.crypto.jdk)
      }
    }
    val iosMain by getting {
      dependencies {
        implementation(libs.crypto.apple)
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(libs.crypto.webcrypto)
      }
    }
  }
}
