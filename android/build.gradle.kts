plugins {
  id("com.android.application")
  kotlin("android")
  id("ozone-android")
  id("ozone-compose")
}

dependencies {
  implementation(project(":common"))
  implementation("androidx.activity:activity-compose:1.7.2")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("androidx.core:core-ktx:1.10.1")
  implementation("dev.marcellogalhardo:retained-activity:1.0.1")
}

android {
  namespace = "sh.christian.ozone"

  defaultConfig {
    applicationId = "sh.christian.ozone"
    versionCode = 100
    versionName = version.toString()
  }
}
