allprojects {
  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  group = "sh.christian.ozone"
  version = "0.0.1-SNAPSHOT"
}

plugins {
  kotlin("multiplatform") apply false
  kotlin("android") apply false
  id("com.android.application") apply false
  id("com.android.library") apply false
  id("org.jetbrains.compose") apply false
}
