![Maven Central](https://img.shields.io/maven-central/v/sh.christian.ozone/bluesky?versionPrefix=0.0.4) ![CI](https://github.com/christiandeange/ozone/actions/workflows/ci.yml/badge.svg)

ozone
=====

### Overview

The Ozone project for the [AT Protocol](https://atproto.com/) consists of 3 components:

1. A Gradle plugin to compile AT Protocol schemas into usable Kotlin classes.
2. Compiled bindings for the [Bluesky Social](https://bsky.app) service.
3. An example Android/Desktop client for Bluesky Social that uses those generated bindings.

> **Warning**
>
> 🚧 🚧 🚧 Everything in here is very much a work-in-progress!
> The [upstream schemas](https://github.com/bluesky-social/atproto/commits/main/lexicons) are still subject to breaking
> changes and may break at any moment if used in production code. Use at your own risk!

### Why "Ozone"?

O<sub>3</sub> exists at all levels in the [ATmosphere](https://bsky.app/profile/shreyanjain.net/post/3k26nw6kwnh2e).

### Bluesky Social Client Apps

#### Usage

```shell
# Desktop app
./gradlew :app:desktop:run

# Android app
./gradlew :app:android:installDebug
```

### Bluesky Social Bindings

#### Usage

```kotlin
// build.gradle[.kts]

dependencies {
  api("sh.christian.ozone:bluesky:0.0.4")
}
```

### Gradle Plugin

#### Usage

```kotlin
// build.gradle[.kts]

plugins {
  id("sh.christian.ozone.generator") version "0.0.4"
}

dependencies {
  // This is where you have your schema files stored in your project.
  lexicons(fileTree("lexicons") { include("**/*.json") })
}

lexicons {
  // Generates an additional interface for the target schemas.
  // This method can be called more than once to generate multiple API interfaces.
  generateApi("BlueskyApi") {
    // Determines the package name of the generated API. Defaults to "sh.christian.ozone".
    packageName.set("com.example.myapp")

    // Generates an additional class that implements this interface by sending corresponding
    // XRPC requests to a provided host conforming to the AT Protocol.
    // Inherits the same package name as the generated interface.
    withKtorImplementation("XrpcBlueskyApi")

    // Determines the return type for each generated API method. Defaults to Raw.
    // - Raw: the raw data type
    // - Result: Result<T>
    // - Response: AtpResponse<T>
    returnType.set(ApiReturnType.Result)

    // Determines whether the generated methods should be marked as suspend functions.
    // When generating a Ktor implementation as well, execution will block the current thread
    // for non-suspending methods. Defaults to true.
    suspending.set(true)
  }

  // File path where Kotlin source files will be written to. Defaults to "/build/generated/lexicons".
  outputDirectory.set(project.layout.buildDirectory.dir("out"))
}
```
