package sh.christian.ozone

import kotlinx.serialization.json.Json
import sh.christian.ozone.api.runtime.buildXrpcJsonConfiguration

/**
 * JSON configuration for serializing and deserializing Bluesky API objects.
 */
val BlueskyJson: Json = buildXrpcJsonConfiguration(Json.serializersModule)
