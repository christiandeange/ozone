plugins {
  id("ozone-multiplatform")
  id("ozone-compose")
}

ozone {
  ios("OzoneIos")
}

kotlin {
  sourceSets {
    iosMain.configure {
      dependencies {
        implementation(project(":app:common"))
        implementation(project(":app:store"))
        implementation(libs.app.platform.renderer.compose)
      }

      resources.srcDir("../common/src/commonMain/composeResources")
    }
  }
}
