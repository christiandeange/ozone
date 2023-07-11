pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}

rootProject.name = "ozone"

include(":android")
include(":api-gen-runtime")
include(":api-gen-runtime-internal")
include(":bluesky")
include(":desktop")
include(":common")
include(":store")

includeBuild("api-gen")
includeBuild("build-logic")
