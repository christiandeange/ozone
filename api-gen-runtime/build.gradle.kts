plugins {
  kotlin("multiplatform")
  id("ozone-base")
  id("ozone-dokka")
  id("ozone-publish")
  kotlin("plugin.serialization")
}

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.immutable)
        api(libs.kotlinx.serialization.cbor)
        api(libs.kotlinx.serialization.json)

        implementation(kotlin("reflect"))
      }
    }
  }
}
