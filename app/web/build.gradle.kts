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

        implementation(npm("buffer", "6.0.3"))
        implementation(npm("process", "0.11.10"))
        implementation(npm("url", "0.11.4"))
      }

      resources.srcDir("../common/src/commonMain/composeResources")
    }
  }
}
