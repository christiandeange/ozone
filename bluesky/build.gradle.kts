import org.jetbrains.dokka.gradle.AbstractDokkaTask
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

tasks.apiDump.configure { dependsOn(tasks.assemble) }
tasks.apiCheck.configure { dependsOn(tasks.assemble) }

tasks.withType<AbstractDokkaTask>().configureEach {
  dependsOn(tasks.generateLexicons)
}
