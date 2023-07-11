@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  kotlin("jvm") version libs.versions.kotlin
  id("ozone-base")
  id("com.github.gmazzo.buildconfig") version libs.versions.buildconfig
  id("com.google.devtools.ksp") version libs.versions.ksp
  `kotlin-dsl`
}

dependencies {
  api(kotlin("gradle-plugin"))
  api(libs.kotlinpoet)
  api(libs.moshi)
  api(libs.moshi.adapters)

  ksp(libs.moshi.codegen)
}

gradlePlugin {
  plugins {
    create("api-gen") {
      id = "sh.christian.ozone.api-gen"
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
    buildConfigField("String", "KTOR_CORE", "\"${libs.ktor.core.get()}\"")
    buildConfigField("String", "KOTLINX_DATETIME", "\"${libs.kotlinx.datetime.get()}\"")
  }
}
