pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }

  plugins {
    kotlin("jvm").version(extra["kotlin.version"] as String) apply false
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
