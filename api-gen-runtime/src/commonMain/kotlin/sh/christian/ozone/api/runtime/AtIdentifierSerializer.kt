package sh.christian.ozone.api.runtime

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import sh.christian.ozone.api.AtIdentifier
import sh.christian.ozone.api.Did
import sh.christian.ozone.api.Handle

object AtIdentifierSerializer : JsonContentPolymorphicSerializer<AtIdentifier>(AtIdentifier::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AtIdentifier> {
    return if (element.jsonPrimitive.content.startsWith("did:")) {
      Did.serializer()
    } else {
      Handle.serializer()
    }
  }
}
