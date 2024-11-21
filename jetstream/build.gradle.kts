import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("ozone-dokka")
  id("ozone-multiplatform")
  id("ozone-publish")
  id("sh.christian.ozone.generator")
  id("org.jetbrains.kotlinx.binary-compatibility-validator")
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

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.bluesky)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.zstd)
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(npm("zstd-codec", "0.1.5"))
      }
    }
  }
}

val generateLexicons = tasks.generateLexicons
tasks.apiDump.configure { dependsOn(generateLexicons) }
tasks.apiCheck.configure { dependsOn(generateLexicons) }

tasks.withType<AbstractDokkaTask>().configureEach {
  dependsOn(tasks.withType<KotlinCompile>())
}
