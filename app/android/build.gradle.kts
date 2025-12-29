plugins {
  id("ozone-multiplatform")
  id("ozone-compose")
}

ozone {
  androidApp {
    namespace = "sh.christian.ozone"

    defaultConfig {
      applicationId = "sh.christian.ozone"
      versionCode = 100
      versionName = version.toString()
    }

    composeOptions {
      kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
  }
}

kotlin {
  sourceSets {
    val androidMain by getting {
      dependencies {
        implementation(project(":app:common"))
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.core)
        implementation(libs.retainedactivity)
      }
    }
  }
}
