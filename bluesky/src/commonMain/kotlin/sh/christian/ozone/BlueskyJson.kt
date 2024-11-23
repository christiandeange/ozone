package sh.christian.ozone

import kotlinx.serialization.json.Json
import sh.christian.ozone.api.runtime.buildXrpcJsonConfiguration
import sh.christian.ozone.api.xrpc.XrpcSerializersModule

/**
 * JSON configuration for serializing and deserializing Bluesky API objects.
 */
val BlueskyJson: Json = buildXrpcJsonConfiguration(XrpcSerializersModule)
