import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("ozone-compose")
}

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(project(":common"))
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
