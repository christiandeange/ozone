import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  `kotlin-dsl`
  id("ozone-base")
  id("ozone-publish")
  id("com.github.gmazzo.buildconfig") version libs.versions.buildconfig
  id("com.google.devtools.ksp") version libs.versions.ksp
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version libs.versions.kotlinx.abi.plugin
}

setProperty("POM_NAME", "AT Protocol for Kotlin Generator")
setProperty("POM_DESCRIPTION", "Gradle Plugin to generate AT Protocol classes.")

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of("17"))
  }
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    apiVersion.set(KotlinVersion.KOTLIN_2_2)
    languageVersion.set(KotlinVersion.KOTLIN_2_2)
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

dependencies {
  api(kotlin("gradle-plugin"))
  api(kotlin("serialization"))

  api(libs.kotlinpoet)
  api(libs.moshi)
  api(libs.moshi.adapters)

  implementation(libs.okio)

  ksp(libs.moshi.codegen)

  testImplementation(kotlin("test"))
}

gradlePlugin {
  plugins {
    create("generator") {
      id = "sh.christian.ozone.generator"
      implementationClass = "sh.christian.ozone.api.gradle.LexiconGeneratorPlugin"
    }
  }
}

buildConfig {
  packageName("sh.christian.ozone.buildconfig")

  useKotlinOutput {
    internalVisibility = true
  }

  forClass("Dependencies") {
    buildConfigField("String", "KOTLINX_SERIALIZATION", "\"${libs.versions.serialization.get()}\"")
    buildConfigField("String", "KTOR", "\"${libs.versions.ktor.get()}\"")
    buildConfigField("String", "OZONE", "\"$version\"")
  }
}
