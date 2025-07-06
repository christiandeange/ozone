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
    val jsMain by getting {
      dependencies {
        implementation(compose.html.core)

        implementation(project(":app:common"))
        implementation(project(":app:store"))
        implementation(project(":bluesky"))

        implementation(npm("process", "0.11.10"))
        implementation(npm("url", "0.11.0"))
      }

      resources.srcDir("../common/src/commonMain/composeResources")
    }
  }
}
