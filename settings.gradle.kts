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

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "ozone"

// Sample app modules
include(":app:android")
include(":app:common")
include(":app:desktop")
include(":app:ios")
include(":app:store")
include(":app:web")

// Published artifact modules
include(":api-gen-runtime")
include(":api-gen-runtime-internal")
include(":bluesky")
include(":lexicons")

includeBuild("build-logic")
includeBuild("generator")
