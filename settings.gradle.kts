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

// Sample app modules
include(":app:android")
include(":app:common")
include(":app:desktop")
include(":app:store")

// Published artifact modules
include(":api-gen-runtime")
include(":api-gen-runtime-internal")
include(":bluesky")

includeBuild("build-logic")
includeBuild("generator")
