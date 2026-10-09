@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.dokka.gradle.tasks.DokkaBaseTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("ozone-dokka")
  id("ozone-multiplatform")
  id("ozone-publish")
  id("sh.christian.ozone.generator")
}

ozone {
  js()
  jvm()
}

dependencies {
  lexicons(fileTree("schemas") {
    include("**/*.json")
  })
}

lexicons {
  defaults {
    generateUnknownsForEnums.set(true)
  }
}

kotlin {
  wasmJs {
    browser {
      commonWebpackConfig {
        configDirectory = project.file("webpack.config.d")
      }
    }
  }

  sourceSets {
    commonMain.configure {
      dependencies {
        api(libs.bluesky)
      }
    }
    jvmMain.configure {
      dependencies {
        implementation(libs.zstd)
      }
    }
    wasmJsMain.configure {
      dependencies {
        implementation(npm("zstd-codec", "0.1.5"))
      }
    }
  }
}

val generateLexicons = tasks.generateLexicons
tasks.apiDump.configure { dependsOn(generateLexicons) }
tasks.apiCheck.configure { dependsOn(generateLexicons) }

tasks.withType<DokkaBaseTask>().configureEach {
  dependsOn(tasks.withType<KotlinCompile>())
}
