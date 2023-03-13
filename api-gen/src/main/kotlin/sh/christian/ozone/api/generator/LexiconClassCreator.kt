package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import sh.christian.ozone.api.generator.builder.BuilderParams
import sh.christian.ozone.api.generator.builder.XrpcBodyGenerator
import sh.christian.ozone.api.generator.builder.XrpcQueryParamsGenerator
import sh.christian.ozone.api.lexicon.LexiconDocument
import sh.christian.ozone.api.lexicon.LexiconDocumentElement
import java.io.File
import kotlin.annotation.AnnotationRetention.RUNTIME

class LexiconClassCreator(
  private val outputDirectory: File,
) {
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
      .writeTo(outputDirectory)
  }

  fun createClassForLexicon(document: LexiconDocument) {
    val inputParams = BuilderParams(
      authority = document.id.substringBeforeLast('.'),
      procedureName = document.id.substringAfterLast('.'),
    )

    val main: LexiconDocumentElement = document.defs["main"]!!
    val userType = (main as LexiconDocumentElement.UserType).userType

    val generators = listOf(
      XrpcQueryParamsGenerator(),
      XrpcBodyGenerator(),
    )

    val codeFile = FileSpec.builder(inputParams.authority, inputParams.procedureName)
      .apply {
        generators
          .flatMap { builder -> builder.generateTypes(inputParams, userType) }
          .forEach { newType -> addType(newType) }
      }
      .build()

    if (codeFile.members.isNotEmpty()) {
      codeFile.writeTo(outputDirectory)
    }
  }
}
