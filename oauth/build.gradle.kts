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
        implementation(libs.securerandom)
      }
    }
  }
}
