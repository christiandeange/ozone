package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.withIndent
import sh.christian.ozone.api.generator.builder.EnumClass
import sh.christian.ozone.api.generator.builder.EnumEntry
import sh.christian.ozone.api.generator.builder.GeneratorContext
import sh.christian.ozone.api.generator.builder.LexiconDataClassesGenerator
import sh.christian.ozone.api.generator.builder.SealedRelationship
import sh.christian.ozone.api.generator.builder.TypesGenerator
import sh.christian.ozone.api.generator.builder.XrpcBodyGenerator
import sh.christian.ozone.api.generator.builder.XrpcQueryParamsGenerator
import sh.christian.ozone.api.generator.builder.capitalized
import sh.christian.ozone.api.generator.builder.createOpenEnumClass
import sh.christian.ozone.api.lexicon.LexiconDocument

class LexiconClassFileCreator(
  private val environment: LexiconProcessingEnvironment,
) {
  private val generators: List<TypesGenerator> = listOf(
    LexiconDataClassesGenerator(environment),
    XrpcQueryParamsGenerator(environment),
    XrpcBodyGenerator(environment),
  )

  private val sealedRelationships = mutableListOf<SealedRelationship>()

  fun createClassForLexicon(document: LexiconDocument) {
    val enums = mutableMapOf<EnumClass, MutableSet<EnumEntry>>()

    document.defs.forEach { (defKey, defValue) ->
      val definitionName = if (defKey == "main") "" else defKey
      val context = GeneratorContext(document, definitionName)

      val codeFile = FileSpec.builder(context.authority, context.procedureName + definitionName.capitalized())
        .apply {
          generators.forEach { builder -> builder.generateTypes(context, defValue) }

          context.enums().forEach { (className, enumNames) ->
            enums.getOrPut(className) { mutableSetOf() } += enumNames
          }
          context.types().forEach { addType(it) }
          context.typeAliases().forEach { addTypeAlias(it) }

          sealedRelationships += context.sealedRelationships()
        }
        .addAnnotation(
          AnnotationSpec.builder(TypeNames.Suppress)
            .addMember("%S", "DEPRECATION")
            .build()
        )
        .build()

      if (codeFile.members.isNotEmpty()) {
        codeFile.writeTo(environment.outputDirectory)
      }
    }

    val context = GeneratorContext(document, "Token")
    val enumFile = FileSpec.builder(context.authority, context.procedureName + "Token")
      .apply {
        enums.forEach { (className, enumNames) ->
          createOpenEnumClass(className, enumNames).forEach { addType(it) }
        }
      }
      .build()

    if (enumFile.members.isNotEmpty()) {
      enumFile.writeTo(environment.outputDirectory)
    }
  }

  fun generateSealedRelationshipMapping() {
    val relationships = sealedRelationships.groupBy { it.sealedInterface }

    FileSpec.builder(findSubscriptionSerializer.packageName, findSubscriptionSerializer.simpleName)
      .addFunction(
        FunSpec.builder(findSubscriptionSerializer)
          .addAnnotation(
            AnnotationSpec.builder(TypeNames.Suppress)
              .addMember("%S", "UNCHECKED_CAST")
              .build()
          )
          .addTypeVariable(TypeVariableName("T", Any::class))
          .addParameter("parentType", TypeNames.KClass.parameterizedBy(TypeVariableName("T")))
          .addParameter("serialName", String::class)
          .returns(
            TypeNames.KSerializer
              .parameterizedBy(TypeVariableName("T", variance = KModifier.OUT))
              .copy(nullable = true)
          )
          .addCode(
            CodeBlock.builder()
              .add("return when(parentType) {\n")
              .withIndent {
                relationships.forEach { (parent, children) ->
                  add("%T::class -> when {\n", parent)
                  withIndent {
                    children.forEach { child ->
                      addStatement(
                        "%S.endsWith(serialName) ->\n%T.serializer()",
                        child.childClassSerialName,
                        child.childClass,
                      )
                    }
                    addStatement("else -> null")
                  }
                  addStatement("}")
                }
                addStatement("else -> null")
              }
              .add(
                "} as %T",
                TypeNames.KSerializer
                  .parameterizedBy(TypeVariableName("T", variance = KModifier.OUT))
                  .copy(nullable = true),
              )
              .build()
          )
          .build()
      )
      .build()
      .writeTo(environment.outputDirectory)
  }
}
