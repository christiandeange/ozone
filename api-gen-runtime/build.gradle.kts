plugins {
  kotlin("multiplatform")
  id("ozone-base")
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
        api(libs.kotlinx.immutable)
        api(libs.kotlinx.serialization.json)

        implementation(kotlin("reflect"))
      }
    }
  }
}
