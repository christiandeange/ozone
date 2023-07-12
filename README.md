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

### Bluesky Social Client App

#### Usage

```shell
# Desktop app
./gradlew :desktop:run

# Android app
./gradlew :android:installDebug
```

### Bluesky Social Bindings

#### Usage

```kotlin
// build.gradle[.kts]

dependencies {
  api("sh.christian.ozone:bluesky:0.0.1-SNAPSHOT")
}
```

### Gradle Plugin

#### Usage

```kotlin
// build.gradle[.kts]

plugins {
  id("sh.christian.ozone.api-gen") version "0.0.1-SNAPSHOT"
}

dependencies {
  // This is where you have your schema files stored in your project.
  lexicons(fileTree("lexicons") { include("**/*.json") })
}

lexicons {
  // Determines if an interface + XRPC implementation should be generated for the target API.
  // Defaults to not generating an API.
  generateApi("BlueskyApi") {
    // Determines the return type for each generated API method. Defaults to Raw.
    // - Raw: the raw data type
    // - Result: Result<T>
    // - Response: AtpResponse<T>
    returnType.set(ApiReturnType.Result)
  }

  // File path where Kotlin source files will be written to.
  // Defaults to /build/generated/lexicons.
  outputDirectory.set(project.layout.buildDirectory.dir("out"))
}
```
