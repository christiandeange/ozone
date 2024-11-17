package sh.christian.ozone.api.runtime

import kotlinx.serialization.json.Json

/**
 * JSON configuration for serializing and deserializing Bluesky API objects.
 */
val BlueskyJson: Json = Json {
  ignoreUnknownKeys = true
  classDiscriminator = "${'$'}type"
}
