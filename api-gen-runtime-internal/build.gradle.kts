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
        api(libs.kotlinx.serialization.json)
        api(libs.ktor.core)

        api(project(":api-gen-runtime"))

        implementation(libs.ktor.contentnegotiation)
        implementation(libs.ktor.serialization.json)

        implementation(kotlin("reflect"))
      }
    }
  }
}
