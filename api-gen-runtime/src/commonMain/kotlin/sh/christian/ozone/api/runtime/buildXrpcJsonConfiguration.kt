package sh.christian.ozone.api.runtime

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

/**
 * JSON configuration for serializing and deserializing lexicon objects with the given module.
 */
fun buildXrpcJsonConfiguration(module: SerializersModule): Json = Json {
  ignoreUnknownKeys = true
  classDiscriminator = "${'$'}type"
  serializersModule = module
}
