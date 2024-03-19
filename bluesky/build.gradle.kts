import com.android.build.gradle.internal.tasks.factory.dependsOn
import sh.christian.ozone.api.generator.ApiReturnType

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
