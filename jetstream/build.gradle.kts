import org.jetbrains.dokka.gradle.AbstractDokkaTask
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
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.bluesky)
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
