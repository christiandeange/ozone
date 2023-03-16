package sh.christian.ozone.api.generator.builder

import sh.christian.ozone.api.lexicon.LexiconUserType

interface TypesGenerator {
  fun generateTypes(
    context: GeneratorContext,
    userType: LexiconUserType,
  )
}
