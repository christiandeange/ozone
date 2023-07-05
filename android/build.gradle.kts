import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.compose")
  id("com.android.application")
  kotlin("android")
}

dependencies {
  implementation(project(":common"))
  implementation("androidx.activity:activity-compose:1.7.2")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("androidx.core:core-ktx:1.10.1")
  implementation("dev.marcellogalhardo:retained-activity:1.0.1")
}

android {
  compileSdk = 33
  namespace = "sh.christian.ozone"

  defaultConfig {
    applicationId = "sh.christian.ozone"
    minSdk = 30
    targetSdk = 33
    versionCode = 100
    versionName = version.toString()
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
tasks.withType<KotlinCompile>().configureEach { kotlinOptions.jvmTarget = "11" }
