@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  kotlin("jvm") version libs.versions.kotlin
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of("17"))
  }
}

dependencies {
  implementation(libs.kotlinx.abi)
  implementation(libs.maven.publish)

  compileOnly(libs.agp)
  compileOnly(libs.dokka)
  compileOnly(kotlin("gradle-plugin"))
}

gradlePlugin {
  plugins {
    create("ozone-android") {
      id = "ozone-android"
      implementationClass = "sh.christian.plugin.BaseAndroidPlugin"
    }

    create("ozone-base") {
      id = "ozone-base"
      implementationClass = "sh.christian.plugin.BasePlugin"
    }

    create("ozone-compose") {
      id = "ozone-compose"
      implementationClass = "sh.christian.plugin.ComposePlugin"
    }

    create("ozone-dokka") {
      id = "ozone-dokka"
      implementationClass = "sh.christian.plugin.DokkaPlugin"
    }

    create("ozone-multiplatform") {
      id = "ozone-multiplatform"
      implementationClass = "sh.christian.plugin.MultiplatformPlugin"
    }

    create("ozone-publish") {
      id = "ozone-publish"
      implementationClass = "sh.christian.plugin.PublishingPlugin"
    }
  }
}
