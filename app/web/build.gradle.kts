plugins {
  kotlin("plugin.serialization")
  id("ozone-multiplatform")
  id("ozone-compose")
}

ozone {
  js()
}

kotlin {
  sourceSets {
    wasmJsMain.configure {
      dependencies {
        implementation(project(":app:common"))
        implementation(project(":app:store"))
        implementation(project(":bluesky"))
        implementation(libs.app.platform.renderer.compose)

      }

      resources.srcDir("../common/src/commonMain/composeResources")
    }
  }
}
