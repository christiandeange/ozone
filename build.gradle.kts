import org.jetbrains.dokka.gradle.DokkaCollectorTask
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  val agp = libs.versions.agp
  val compose = libs.versions.compose
  val kotlinCompose = libs.versions.kotlincompose
  val dokka = libs.versions.dokka
  val kmmbridge = libs.versions.kmmbridge
  val kotlin = libs.versions.kotlin
  val ksp = libs.versions.ksp
  val mavenPublish = libs.versions.maven.publish
  val kotlinxAbi = libs.versions.kotlinx.abi.plugin
  val skie = libs.versions.skie

  kotlin("jvm") version kotlin apply false
  kotlin("multiplatform") version kotlin apply false
  kotlin("plugin.serialization") version kotlin apply false
  kotlin("android") version kotlin apply false
  id("co.touchlab.kmmbridge") version kmmbridge apply false
  id("co.touchlab.skie") version skie apply false
  id("com.android.application") version agp apply false
  id("com.android.library") version agp apply false
  id("com.google.devtools.ksp") version ksp apply false
  id("com.vanniktech.maven.publish") version mavenPublish apply false
  id("org.jetbrains.compose") version compose apply false
  id("org.jetbrains.dokka") version dokka apply true
  id("org.jetbrains.kotlin.plugin.compose") version kotlinCompose apply false
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version kotlinxAbi apply false
}

allprojects {
  group = property("POM_GROUP_ID").toString()
  version = property("POM_VERSION").toString()

  configurations.all {
    resolutionStrategy.dependencySubstitution {
      substitute(module("$group:api-gen-runtime:$version"))
        .using(project(":api-gen-runtime"))

      substitute(module("$group:api-gen-runtime-internal:$version"))
        .using(project(":api-gen-runtime-internal"))
    }
  }
}

tasks.withType<DokkaMultiModuleTask>().configureEach {
  outputDirectory.set(file("$rootDir/docs"))
}

tasks.withType<DokkaCollectorTask>().configureEach {
  outputDirectory.set(file("$rootDir/docs"))
}
