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
        api("io.ktor:ktor-client-core:2.3.2")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

        api(project(":api-gen-runtime-api"))

        implementation("io.ktor:ktor-client-content-negotiation:2.3.2")
        implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.2")

        implementation(kotlin("reflect"))
      }
    }
  }
}
