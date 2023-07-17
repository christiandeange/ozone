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

rootProject.name = "generator"

include(":api-gen-runtime")
include(":api-gen-runtime-internal")

includeBuild("../build-logic")
