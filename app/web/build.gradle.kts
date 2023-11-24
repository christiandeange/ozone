import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("ozone-compose")
}

kotlin {
  js(IR) {
    browser()
    nodejs()
    binaries.executable()
  }
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
