import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("plugin.serialization")
  id("ozone-multiplatform")
  id("ozone-compose")
}

ozone {
  jvm()
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(project(":app:common"))
        implementation(compose.desktop.currentOs)
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "sh.christian.ozone.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "ozone"
      packageVersion = "1.0.0"
    }
  }
}
