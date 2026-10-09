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
    commonMain.configure {
      dependencies {
        api(libs.kotlinx.serialization.json)
        api(libs.ktor.core)

        implementation(kotlin("reflect"))
      }
    }
    iosMain.configure {
      dependencies {
        implementation(libs.ktor.darwin)
      }
    }
    jvmMain.configure {
      dependencies {
        implementation(libs.ktor.cio)
      }
    }
    wasmJsMain.configure {
      dependencies {
        implementation(libs.ktor.js)
      }
    }
  }
}
