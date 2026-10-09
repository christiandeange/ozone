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
    commonMain.configure {
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
    iosMain.configure {
      dependencies {
        implementation(libs.crypto.apple)
      }
    }
    jvmMain.configure {
      dependencies {
        implementation(libs.crypto.jdk)
      }
    }
    wasmJsMain.configure {
      dependencies {
        implementation(libs.crypto.webcrypto)
      }
    }
  }
}
