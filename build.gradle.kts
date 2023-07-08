allprojects {
  group = property("POM_GROUP_ID").toString()
  version = property("POM_VERSION").toString()
}

plugins {
  kotlin("jvm") apply false
  kotlin("multiplatform") apply false
  kotlin("plugin.serialization") apply false
  kotlin("android") apply false
  id("com.android.application") apply false
  id("com.android.library") apply false
  id("com.vanniktech.maven.publish") apply false
  id("org.jetbrains.compose") apply false
}
