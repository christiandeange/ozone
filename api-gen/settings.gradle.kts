pluginManagement {
  val kotlinVersion: String by settings
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }

  plugins {
    kotlin("jvm").version(kotlinVersion) apply false
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "api-gen"
