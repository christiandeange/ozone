package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.STRING
import sh.christian.ozone.api.generator.TypeNames
import sh.christian.ozone.api.lexicon.LexiconBoolean
import sh.christian.ozone.api.lexicon.LexiconFloat
import sh.christian.ozone.api.lexicon.LexiconInteger
import sh.christian.ozone.api.lexicon.LexiconPrimitive
import sh.christian.ozone.api.lexicon.LexiconString
import sh.christian.ozone.api.lexicon.LexiconStringFormat
import sh.christian.ozone.api.lexicon.LexiconUnknown

fun GeneratorContext.primitiveDefaultValue(
  primitive: LexiconPrimitive,
  propertyName: String,
): CodeBlock? = when (primitive) {
  is LexiconBoolean -> primitive.default?.let { CodeBlock.of("%L", it) }
  is LexiconFloat -> primitive.default?.let { CodeBlock.of("%Lf", it) }
  is LexiconInteger -> primitive.default?.let { CodeBlock.of("%L", it) }
  is LexiconString -> {
    primitive.default?.let { default ->
      if (primitive.isEnumValues()) {
        val enumClassName = ClassName(authority, classPrefix + propertyName.capitalized())
        CodeBlock.of("%T.%N", enumClassName, default.substringAfterLast('#').toPascalCase())
      } else {
        when (primitive.format) {
          LexiconStringFormat.DATETIME -> CodeBlock.of("%T(%S)", TypeNames.Timestamp, default)
          LexiconStringFormat.URI -> CodeBlock.of("%T(%S)", TypeNames.Uri, default)
          LexiconStringFormat.AT_URI -> CodeBlock.of("%T(%S)", TypeNames.AtUri, default)
          LexiconStringFormat.DID -> CodeBlock.of("%T(%S)", TypeNames.Did, default)
          LexiconStringFormat.HANDLE -> CodeBlock.of("%T(%S)", TypeNames.Handle, default)
          LexiconStringFormat.AT_IDENTIFIER -> CodeBlock.of("%T(%S)", TypeNames.AtIdentifier, default)
          LexiconStringFormat.NSID -> CodeBlock.of("%T(%S)", TypeNames.Nsid, default)
          LexiconStringFormat.CID -> CodeBlock.of("%T(%S)", TypeNames.Cid, default)
          LexiconStringFormat.LANGUAGE -> CodeBlock.of("%T(%S)", TypeNames.Language, default)
          LexiconStringFormat.TID -> CodeBlock.of("%T(%S)", TypeNames.Tid, default)
          LexiconStringFormat.RECORD_KEY -> CodeBlock.of("%T(%S)", TypeNames.RKey, default)
          null -> CodeBlock.of("%S", default)
        }
      }
    }
  }
  is LexiconUnknown -> null
}

fun GeneratorContext.primitiveTypeName(
  primitive: LexiconPrimitive,
  propertyName: String,
) = when (primitive) {
  is LexiconBoolean -> BOOLEAN
  is LexiconInteger -> LONG
  is LexiconFloat -> DOUBLE
  is LexiconString -> {
    if (primitive.isEnumValues()) {
      ClassName(authority, classPrefix + propertyName.capitalized())
    } else {
      when (primitive.format) {
        LexiconStringFormat.DATETIME -> TypeNames.Timestamp
        LexiconStringFormat.URI -> TypeNames.Uri
        LexiconStringFormat.AT_URI -> TypeNames.AtUri
        LexiconStringFormat.DID -> TypeNames.Did
        LexiconStringFormat.HANDLE -> TypeNames.Handle
        LexiconStringFormat.AT_IDENTIFIER -> TypeNames.AtIdentifier
        LexiconStringFormat.NSID -> TypeNames.Nsid
        LexiconStringFormat.CID -> TypeNames.Cid
        LexiconStringFormat.LANGUAGE -> TypeNames.Language
        LexiconStringFormat.TID -> TypeNames.Tid
        LexiconStringFormat.RECORD_KEY -> TypeNames.RKey
        null -> STRING
      }
    }
  }
  is LexiconUnknown -> TypeNames.JsonContent
}

fun LexiconString.isEnumValues(): Boolean {
  return knownValues.isNotEmpty() && knownValues.none { '#' in it }
}

fun LexiconString.isEnumReference(): Boolean {
  return knownValues.isNotEmpty() && knownValues.all { '#' in it }
}
