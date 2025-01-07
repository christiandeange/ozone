![Maven Central](https://img.shields.io/maven-central/v/sh.christian.ozone/bluesky?versionPrefix=0.3.1) ![CI](https://github.com/christiandeange/ozone/actions/workflows/ci.yml/badge.svg)

ozone
=====

### Overview

The Ozone project for the [AT Protocol](https://atproto.com/) consists of 4 components:

1. A Gradle plugin to compile AT Protocol schemas into usable Kotlin classes.
2. Multiplatform APIs for the AT Protocol spec that can be used with any compatible service, including [Bluesky Social](https://bsky.app).
   - Supports Android, JVM, JavaScript, and iOS.
3. APIs to connect to the [Jetstream](https://github.com/bluesky-social/jetstream) firehose for Bluesky Social.
   - Supports Android, JVM, and JavaScript.
4. Example client apps that demonstrate usage of those APIs.

> **Warning**
>
> 🚧 🚧 🚧 Everything in here is very much a work-in-progress!
> The [upstream schemas](https://github.com/bluesky-social/atproto/commits/main/lexicons) are still subject to breaking
> changes and may break at any moment if used in production code. Use at your own risk!

### Why "Ozone"?

O<sub>3</sub> exists at all levels in the [ATmosphere](https://bsky.app/profile/shreyanjain.net/post/3k26nw6kwnh2e).

No relation to the moderation tools also named [Ozone](https://github.com/bluesky-social/ozone).

### Bluesky Social Bindings

Documentation is available at [ozone.christian.sh](https://ozone.christian.sh).

#### Java / Kotlin

```kotlin
// build.gradle[.kts]

dependencies {
  api("sh.christian.ozone:bluesky:0.3.1")
}
```

#### Swift / Objective-C

In Xcode, select **File > Add Packages** and enter https://github.com/christiandeange/BlueskyAPI

### Jetstream Bindings

Documentation is available at [ozone.christian.sh](https://ozone.christian.sh/ozone/sh.christian.ozone.jetstream/index.html).

#### Java / Kotlin

```kotlin
// build.gradle[.kts]

dependencies {
  api("sh.christian.ozone:jetstream:0.3.1")
}
```

### Gradle Plugin

In addition to shipping the lexicons that define the official Bluesky API, this project also includes a Gradle Plugin that allows you to bring your own lexicon definitions and generate any set of AT Protocol bindings from them.

```kotlin
// build.gradle[.kts]

plugins {
  id("sh.christian.ozone.generator") version "0.3.1"
}

dependencies {
  // This is where you have your schema files stored in your project.
  lexicons(fileTree("lexicons") { include("**/*.json") })

  // You can also depend directly on the published Bluesky lexicons instead.
  lexicons("sh.christian.ozone:lexicons:0.3.1")
}

lexicons {
  // Determines the package name of generated supporting methods. Defaults to "sh.christian.ozone".
  namespace.set("com.example.myapp")

   // Configuration for how to handle unknown types and known values.
  defaults {
     // Determines whether to generate classes to encapsulate unknown types for union references. Defaults to false.
     generateUnknownsForSealedTypes.set(true)

     // Determines whether to generate classes to encapsulate unknown values for strings. Defaults to false.
     generateUnknownsForEnums.set(true)
  }

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

  // File path where Kotlin source files will be written to. Defaults to "<project-dir>/build/generated/lexicons".
  outputDirectory.set(project.layout.buildDirectory.dir("out"))
}
```
