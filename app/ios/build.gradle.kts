plugins {
  id("ozone-multiplatform")
  id("ozone-compose")
}

ozone {
  ios("OzoneIos")
}

kotlin {
  sourceSets {
    val iosMain by getting {
      dependencies {
        implementation(project(":app:common"))
        implementation(project(":app:store"))
      }

      resources.srcDir("../common/src/commonMain/composeResources")
    }
  }
}
