import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  kotlin("plugin.serialization")
  id("ozone-multiplatform")
  id("ozone-compose")
  id("com.google.devtools.ksp")
}

ozone {
  androidLibrary {
    namespace = "sh.christian.ozone.common"
  }
  js()
  jvm()
  ios("OzoneCommon")
}

kotlin {
  @Suppress("OPT_IN_USAGE")
  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }

  sourceSets {
    val commonMain by getting {
      kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

      dependencies {
        api(compose.foundation)
        api(compose.material3)
        api(compose.runtime)

        api(libs.kotlinx.immutable)
        api(libs.kotlinx.serialization.core)
        api(libs.workflow.core)
        api(libs.workflow.runtime)

        implementation(libs.codepoints.deluxe)
        implementation(compose.components.resources)
        implementation(libs.kamel)
        implementation(libs.kotlininject)
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
    val androidMain by getting {
      dependencies {
        implementation(libs.androidx.activity.compose)
        implementation(libs.zoomable)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.apache.commons)
        implementation(libs.zoomable)
      }
    }
  }
}

dependencies {
  add("kspCommonMainMetadata", libs.kotlininject.compiler)
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
  if (name != "kspCommonMainKotlinMetadata") {
    dependsOn("kspCommonMainKotlinMetadata")
  }
}
