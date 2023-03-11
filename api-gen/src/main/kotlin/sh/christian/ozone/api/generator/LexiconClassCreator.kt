package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.configurationcache.extensions.capitalized
import sh.christian.ozone.api.lexicon.LexiconArrayItem
import sh.christian.ozone.api.lexicon.LexiconDocument
import sh.christian.ozone.api.lexicon.LexiconDocumentElement
import sh.christian.ozone.api.lexicon.LexiconObjectProperty
import sh.christian.ozone.api.lexicon.LexiconPrimitive
import sh.christian.ozone.api.lexicon.LexiconXrpcBody
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery
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
    val authority = document.id.substringBeforeLast('.')
    val procedureName = document.id.substringAfterLast('.')
    val classPrefix = procedureName.capitalized()

    val main: LexiconDocumentElement = document.defs["main"]!!
    val userType = (main as LexiconDocumentElement.UserType).userType

    FileSpec.builder(authority, procedureName)
      .apply {
        if (userType is LexiconXrpcProcedure) {
          if (userType.parameters.isNotEmpty()) {
            addType(createQueryParamsType("${classPrefix}QueryParams", userType.parameters))
          }
          userType.input?.let {
            addType(createBodyType("${classPrefix}Request", it))
          }
          userType.output?.let {
            addType(createBodyType("${classPrefix}Response", it))
          }
        } else if (userType is LexiconXrpcQuery) {
          if (userType.parameters.isNotEmpty()) {
            addType(createQueryParamsType("${classPrefix}QueryParams", userType.parameters))
          }
          userType.output?.let {
            addType(createBodyType("${classPrefix}Response", it))
          }
        }
      }
      .build()
      .writeTo(outputDirectory)
  }

  private fun createQueryParamsType(
    className: String,
    queryParams: Map<String, LexiconPrimitive>,
  ): TypeSpec {
    val properties: List<Property> = queryParams.map { (name, prop) ->
      Property(name, prop.type.toKotlinPoetTypeName(nullable = false))
    }

    return createDataClass(
      className = className,
      properties = properties,
    )
  }

  private fun createBodyType(
    className: String,
    body: LexiconXrpcBody,
  ): TypeSpec {
    val encodings: List<String> = body.encoding.values()

    val properties: List<Property> = body.schema.properties.map { (name, prop) ->
      val isNullable = name !in body.schema.required

      when (prop) {
        is LexiconObjectProperty.Array -> {
          when (val items = prop.array.items) {
            is LexiconArrayItem.Primitive -> {
              val propertyType = items.primitive.type.toKotlinPoetTypeName(nullable = false)
              Property(name, LIST.parameterizedBy(propertyType).copy(isNullable))
            }

            is LexiconArrayItem.Reference -> TODO()
            is LexiconArrayItem.ReferenceList -> TODO()
          }
        }

        is LexiconObjectProperty.Primitive -> {
          Property(name, prop.primitive.type.toKotlinPoetTypeName(isNullable))
        }

        is LexiconObjectProperty.Reference -> TODO()
        is LexiconObjectProperty.ReferenceList -> TODO()
      }
    }

    return createDataClass(
      className = className,
      properties = properties,
      additionalConfiguration = {
        addAnnotation(SERIALIZABLE)
        addAnnotation(
          AnnotationSpec.builder(ENCODING)
            .addMember(encodings.joinToString(", ") { "%S" }, *encodings.toTypedArray())
            .build()
        )
      },
    )
  }

  private fun createDataClass(
    className: String,
    properties: List<Property>,
    additionalConfiguration: TypeSpec.Builder.() -> Unit = {},
  ): TypeSpec {
    return TypeSpec.classBuilder(className)
      .addModifiers(KModifier.DATA)
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameters(
            properties.map { property ->
              ParameterSpec
                .builder(property.name, property.type)
                .build()
            }
          )
          .build()
      )
      .addProperties(
        properties.map { property ->
          PropertySpec
            .builder(property.name, property.type)
            .initializer(property.name)
            .build()
        }
      )
      .apply(additionalConfiguration)
      .build()
  }

  private data class Property(
    val name: String,
    val type: TypeName,
  )

  companion object {
    private val ENCODING = ClassName("sh.christian.ozone.api", "Encoding")
    private val SERIALIZABLE = ClassName("kotlinx.serialization", "Serializable")
  }
}

private fun LexiconPrimitive.Type.toKotlinPoetTypeName(
  nullable: Boolean,
) = when (this) {
  LexiconPrimitive.Type.BOOLEAN -> BOOLEAN.copy(nullable = nullable)
  LexiconPrimitive.Type.NUMBER -> LONG.copy(nullable = nullable)
  LexiconPrimitive.Type.INTEGER -> INT.copy(nullable = nullable)
  LexiconPrimitive.Type.STRING -> STRING.copy(nullable = nullable)
}
