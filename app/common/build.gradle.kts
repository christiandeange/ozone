plugins {
  kotlin("plugin.serialization")
  id("ozone-multiplatform")
  id("ozone-compose")
  alias(libs.plugins.app.platform)
}

appPlatform {
  addPublicModuleDependencies(true)
  addImplModuleDependencies(true)
  enableKotlinInject(true)
  enableComposePresenters(true)
  enableComposeUi(true)
}

ozone {
  androidLibrary {
    namespace = "sh.christian.ozone.common"

    composeOptions {
      kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
  }
  js()
  jvm()
  ios("OzoneCommon")
}

kotlin {
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    nodejs {
      // Skiko's Wasm runtime is browser-only; run the shared Wasm tests in the browser.
      testTask {
        enabled = false
      }
    }
  }

  @Suppress("OPT_IN_USAGE")
  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }

  sourceSets {
    commonMain.configure {
      dependencies {
        api(compose.foundation)
        api(compose.material3)
        api(compose.runtime)

        api(libs.kotlinx.immutable)
        api(libs.kotlinx.serialization.core)

        implementation(libs.codepoints.deluxe)
        implementation(compose.components.resources)
        implementation(libs.kamel)
        implementation(libs.kotlinx.atomicfu)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.ktor.logging)
        // Uncomment to fetch all icons.
        // implementation(libs.material.icons.extended)
        implementation(libs.material.icons.core)

        api(project(":bluesky"))
        api(project(":app:store"))

        runtimeOnly(libs.slf4j.simple)
      }
    }
    androidMain.configure {
      dependencies {
        implementation(libs.androidx.activity.compose)
        implementation(libs.zoomable)
      }
    }
    jvmMain.configure {
      dependencies {
        implementation(libs.apache.commons)
        implementation(libs.zoomable)
      }
    }
    commonTest.configure {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}
