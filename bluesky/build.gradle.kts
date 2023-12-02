import com.android.build.gradle.internal.tasks.factory.dependsOn
import sh.christian.ozone.api.generator.ApiReturnType

plugins {
  kotlin("multiplatform")
  id("ozone-base")
  id("ozone-publish")
  id("sh.christian.ozone.generator")
  id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }
  js(IR) {
    browser()
    nodejs()
    binaries.executable()
  }
}

dependencies {
  lexicons(fileTree("lexicons") { include("**/*.json") })
}

lexicons {
  generateApi("BlueskyApi") {
    withKtorImplementation("XrpcBlueskyApi")
    returnType.set(ApiReturnType.Response)
  }
}

tasks.apiDump.dependsOn(tasks.assemble)
tasks.apiCheck.dependsOn(tasks.assemble)
