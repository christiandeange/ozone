package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.configurationcache.extensions.capitalized
import sh.christian.ozone.api.generator.builder.GeneratorContext
import sh.christian.ozone.api.generator.builder.LexiconDataClassesGenerator
import sh.christian.ozone.api.generator.builder.TypesGenerator
import sh.christian.ozone.api.generator.builder.XrpcBodyGenerator
import sh.christian.ozone.api.generator.builder.XrpcQueryParamsGenerator
import sh.christian.ozone.api.generator.builder.createEnumClass
import sh.christian.ozone.api.lexicon.LexiconDocument
import kotlin.annotation.AnnotationRetention.RUNTIME

class LexiconClassFileCreator(
  private val environment: LexiconProcessingEnvironment,
) {
  private val generators: List<TypesGenerator> = listOf(
    LexiconDataClassesGenerator(environment),
    XrpcQueryParamsGenerator(environment),
    XrpcBodyGenerator(environment),
  )

  fun createCommonClasses() {
    FileSpec.builder("sh.christian.ozone.api", "common")
      .addType(
        TypeSpec.annotationBuilder(ENCODING)
          .addAnnotation(
            AnnotationSpec.builder(Retention::class)
              .addMember("%T.%L", AnnotationRetention::class, RUNTIME)
              .build()
          )
          .primaryConstructor(
            FunSpec.constructorBuilder()
              .addParameter(ParameterSpec.builder("type", STRING, KModifier.VARARG).build())
              .build()
          )
          .addProperty(
            PropertySpec.builder("type", STRING)
              .initializer("type")
              .build()
          )
          .build()
      )
      .build()
      .writeTo(environment.outputDirectory)
  }

  fun createClassForLexicon(document: LexiconDocument) {
    val enums = mutableMapOf<ClassName, MutableSet<String>>()

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
        }
        .build()

      if (codeFile.members.isNotEmpty()) {
        codeFile.writeTo(environment.outputDirectory)
      }
    }

    val context = GeneratorContext(document, "Enum")
    val enumFile = FileSpec.builder(context.authority, context.procedureName + "Enum")
      .apply {
        enums.forEach { (className, enumNames) ->
          addType(createEnumClass(className, enumNames))
        }
      }
      .build()

    if (enumFile.members.isNotEmpty()) {
      enumFile.writeTo(environment.outputDirectory)
    }
  }
}
