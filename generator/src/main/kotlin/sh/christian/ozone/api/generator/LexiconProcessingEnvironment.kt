package sh.christian.ozone.api.generator

import sh.christian.ozone.api.generator.builder.parseLexiconRef
import sh.christian.ozone.api.lexicon.LexiconDocument
import sh.christian.ozone.api.lexicon.LexiconSingleReference
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.parseDocument
import sh.christian.ozone.api.lexicon.parseDocumentMetadata
import java.io.File

class LexiconProcessingEnvironment(
  allLexiconSchemaJsons: List<String>,
  val outputDirectory: File,
) : Iterable<String> {
  private val schemasById: Map<String, String>
  private val schemaCache = mutableMapOf<String, LexiconDocument>()

  init {
    schemasById = allLexiconSchemaJsons.associateBy { parseDocumentMetadata(it).id }
  }

  fun loadDocument(schemaId: String): LexiconDocument {
    return schemaCache.getOrPut(schemaId) {
      parseDocument(schemasById[schemaId]!!)
    }
  }

  fun loadReferenceDocument(
    source: LexiconDocument,
    reference: LexiconSingleReference,
  ): LexiconDocument {
    val (lexiconId, _) = reference.ref.parseLexiconRef(source)

    return if (lexiconId.isEmpty()) {
      source
    } else {
      loadDocument(lexiconId)
    }
  }

  fun loadReference(
    source: LexiconDocument,
    reference: LexiconSingleReference,
  ): LexiconUserType {
    val (lexiconId, objectRef) = reference.ref.parseLexiconRef(source)

    val lexiconDocument = if (lexiconId.isEmpty()) {
      source
    } else {
      loadDocument(lexiconId)
    }

    return lexiconDocument.defs[objectRef]!!
  }

  override fun iterator(): Iterator<String> = schemasById.keys.iterator()
}
