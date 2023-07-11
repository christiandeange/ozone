pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }

  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "api-gen"

include(":api-gen-runtime-api")
include(":api-gen-runtime-implementation")

includeBuild("../build-logic")
