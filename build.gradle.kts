allprojects {
  group = "sh.christian.ozone"
  version = "0.0.1-SNAPSHOT"
}

plugins {
  kotlin("jvm") apply false
  kotlin("multiplatform") apply false
  kotlin("plugin.serialization") apply false
  kotlin("android") apply false
  id("com.android.application") apply false
  id("com.android.library") apply false
  id("org.jetbrains.compose") apply false
}
