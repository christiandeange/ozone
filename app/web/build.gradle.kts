import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

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

      resources.srcDir("../common/fonts")
    }
  }
}

tasks.withType<KotlinWebpack>().configureEach {
  devServer?.port = 3000
}

compose.experimental {
  web.application {}
}
