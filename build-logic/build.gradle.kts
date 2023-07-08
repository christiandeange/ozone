import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  kotlin("jvm")
}

val agpVersion by project.properties
val kotlinVersion by project.properties

dependencies {
  implementation("com.vanniktech:gradle-maven-publish-plugin:0.25.3")
  compileOnly("com.android.tools.build:gradle-api:$agpVersion")
  compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}

tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
tasks.withType<KotlinCompile>().configureEach { kotlinOptions.jvmTarget = "11" }

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of("11"))
  }
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
