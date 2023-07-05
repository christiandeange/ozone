plugins {
  kotlin("jvm")
  id("com.google.devtools.ksp") version "1.8.20-1.0.11"
  `kotlin-dsl`
}

dependencies {
  api(kotlin("gradle-plugin"))
  api("com.squareup:kotlinpoet:1.14.2")
  api("com.squareup.moshi:moshi:1.15.0")
  api("com.squareup.moshi:moshi-adapters:1.15.0")

  ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of("11"))
  }
}

gradlePlugin {
  plugins {
    create("api-gen") {
      id = "sh.christian.ozone.api-gen"
      implementationClass = "sh.christian.ozone.api.gradle.LexiconGeneratorPlugin"
    }
  }
}
