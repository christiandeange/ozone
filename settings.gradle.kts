pluginManagement {
  val kotlinVersion: String by settings
  val agpVersion: String by settings
  val composeVersion: String by settings

  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  plugins {
    kotlin("jvm").version(kotlinVersion) apply false
    kotlin("multiplatform").version(kotlinVersion) apply false
    kotlin("plugin.serialization").version(kotlinVersion) apply false
    kotlin("android").version(kotlinVersion) apply false
    id("com.android.application").version(agpVersion) apply false
    id("com.android.library").version(agpVersion) apply false
    id("com.vanniktech.maven.publish").version("0.25.3") apply false
    id("org.jetbrains.compose").version(composeVersion) apply false
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
include(":api-gen-runtime-api")
include(":api-gen-runtime-implementation")
include(":bluesky")
include(":desktop")
include(":common")
include(":store")

includeBuild("api-gen")
includeBuild("build-logic")
