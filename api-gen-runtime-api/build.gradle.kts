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
        api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

        implementation(kotlin("reflect"))
      }
    }
  }
}
