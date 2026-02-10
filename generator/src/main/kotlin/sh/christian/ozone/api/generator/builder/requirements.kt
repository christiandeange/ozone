package sh.christian.ozone.api.generator.builder

import sh.christian.ozone.api.lexicon.LexiconArray
import sh.christian.ozone.api.lexicon.LexiconArrayItem
import sh.christian.ozone.api.lexicon.LexiconBlob
import sh.christian.ozone.api.lexicon.LexiconBoolean
import sh.christian.ozone.api.lexicon.LexiconBytes
import sh.christian.ozone.api.lexicon.LexiconCidLink
import sh.christian.ozone.api.lexicon.LexiconFloat
import sh.christian.ozone.api.lexicon.LexiconInteger
import sh.christian.ozone.api.lexicon.LexiconIpldType
import sh.christian.ozone.api.lexicon.LexiconObjectProperty
import sh.christian.ozone.api.lexicon.LexiconPrimitive
import sh.christian.ozone.api.lexicon.LexiconPrimitiveArray
import sh.christian.ozone.api.lexicon.LexiconReference
import sh.christian.ozone.api.lexicon.LexiconString
import sh.christian.ozone.api.lexicon.LexiconUnknown

fun LexiconObjectProperty.requirements(): List<Requirement> = when (this) {
  is LexiconObjectProperty.Array -> array.requirements()
  is LexiconObjectProperty.Blob -> blob.requirements()
  is LexiconObjectProperty.IpldType -> ipld.requirements()
  is LexiconObjectProperty.Primitive -> primitive.requirements()
  is LexiconObjectProperty.Reference -> reference.requirements()
}

fun LexiconArrayItem.requirements(): List<Requirement> = when (this) {
  is LexiconArrayItem.Blob -> blob.requirements()
  is LexiconArrayItem.IpldType -> ipld.requirements()
  is LexiconArrayItem.Primitive -> primitive.requirements()
  is LexiconArrayItem.Reference -> reference.requirements()
}

fun LexiconArray.requirements(): List<Requirement> = listOfNotNull(
  minLength?.let(Requirement::MinLength),
  maxLength?.let(Requirement::MaxLength),
) + items.requirements()

fun LexiconPrimitiveArray.requirements(): List<Requirement> = listOfNotNull(
  minLength?.let(Requirement::MinLength),
  maxLength?.let(Requirement::MaxLength),
) + items.requirements()

fun LexiconReference.requirements(): List<Requirement> = emptyList()

fun LexiconBlob.requirements(): List<Requirement> = emptyList()
// TODO enforce maxSize value when blobs are parsed to a proper data model, not a JsonElement.
// listOfNotNull(
//  maxSize?.toLong()?.let(Requirement::MaxLength),
//)

fun LexiconIpldType.requirements(): List<Requirement> = when (this) {
  is LexiconBytes -> {
    listOfNotNull(
      minLength?.toLong()?.let(Requirement::MinLength),
      maxLength?.toLong()?.let(Requirement::MaxLength),
    )
  }
  is LexiconCidLink -> emptyList()
}

fun LexiconPrimitive.requirements(): List<Requirement> = when (this) {
  is LexiconBoolean -> emptyList()
  is LexiconFloat -> {
    listOfNotNull(
      minimum?.let(Requirement::MinValue),
      maximum?.let(Requirement::MaxValue),
    )
  }
  is LexiconInteger -> {
    listOfNotNull(
      minimum?.let(Requirement::MinValue),
      maximum?.let(Requirement::MaxValue),
    )
  }
  is LexiconString -> {
    if (knownValues.isNotEmpty()) {
      listOfNotNull(
        minLength?.let(Requirement::MinToStringLength),
        maxLength?.let(Requirement::MaxToStringLength),
      )
    } else {
      listOfNotNull(
        minLength?.let(Requirement::MinLength),
        maxLength?.let(Requirement::MaxLength),
      )
    }
  }
  is LexiconUnknown -> emptyList()
}
