plugins {
  kotlin("multiplatform")
  id("ozone-base")
  id("ozone-publish")
  id("sh.christian.ozone.api-gen")
}

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }
}

dependencies {
  lexicons(fileTree("lexicons") { include("**/*.json") })
}

lexicons {
  apiName.set("BlueskyApi")
}
