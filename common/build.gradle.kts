plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
  id("com.android.library")
}

kotlin {
  android()
  jvm("desktop") {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(compose.runtime)
        api(compose.foundation)
        api(compose.material)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    val androidMain by getting {
      dependencies {
        api("androidx.appcompat:appcompat:1.6.1")
        api("androidx.core:core-ktx:1.9.0")
      }
    }
    val androidUnitTest by getting {
      dependencies {
        implementation("junit:junit:4.13.2")
      }
    }
    val desktopMain by getting {
      dependencies {
        api(compose.preview)
      }
    }
    val desktopTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}

android {
  compileSdk = 33
  namespace = "sh.christian.ozone.common"

  defaultConfig {
    minSdk = 24
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}
