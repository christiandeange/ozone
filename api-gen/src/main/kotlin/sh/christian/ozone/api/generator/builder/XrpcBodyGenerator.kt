package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.configurationcache.extensions.capitalized
import sh.christian.ozone.api.generator.ENCODING
import sh.christian.ozone.api.generator.LexiconProcessingEnvironment
import sh.christian.ozone.api.lexicon.LexiconArrayItem
import sh.christian.ozone.api.lexicon.LexiconObject
import sh.christian.ozone.api.lexicon.LexiconObjectProperty
import sh.christian.ozone.api.lexicon.LexiconSingleReference
import sh.christian.ozone.api.lexicon.LexiconUnionReference
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.LexiconXrpcBody
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery
import sh.christian.ozone.api.lexicon.LexiconXrpcSchemaDefinition

class XrpcBodyGenerator(
  private val environment: LexiconProcessingEnvironment,
) : TypesGenerator {

  override fun generateTypes(
    context: GeneratorContext,
    userType: LexiconUserType,
  ) {
    when (userType) {
      is LexiconXrpcProcedure -> {
        userType.input?.let {
          createBodyType(context, "${context.classPrefix}Request", it)
        }
        userType.output?.let {
          createBodyType(context, "${context.classPrefix}Response", it)
        }
      }
      is LexiconXrpcQuery -> {
        userType.output?.let {
          createBodyType(context, "${context.classPrefix}Response", it)
        }
      }
      else -> {
        println("Skipping: $userType")
      }
    }
  }

  private fun createBodyType(
    context: GeneratorContext,
    className: String,
    body: LexiconXrpcBody,
  ) {
    when (body.schema) {
      null -> Unit
      is LexiconXrpcSchemaDefinition.Object -> {
        context.addType(
          createType(context, className, body.schema.value)
            .forRequestOrResponse(body.encoding)
        )
      }
      is LexiconXrpcSchemaDefinition.Reference -> {
        val typeAliasType = when (body.schema.reference) {
          is LexiconSingleReference -> body.schema.reference.typeName(environment, context.document)
          is LexiconUnionReference -> ClassName(context.authority, className.capitalized())
        }
        context.addTypeAlias(TypeAliasSpec.builder(className, typeAliasType).build())
      }
    }
  }

  private fun createType(
    context: GeneratorContext,
    className: String,
    body: LexiconObject,
  ): TypeSpec {
    val properties: List<SimpleProperty> = body.properties.map { (name, prop) ->
      val isNullable = name in body.nullable

      when (prop) {
        is LexiconObjectProperty.Array -> {
          SimpleProperty(
            name = name,
            type = when (prop.array.items) {
              is LexiconArrayItem.Primitive -> {
                prop.array.items.primitive.toTypeName(nullable = isNullable)
              }
              is LexiconArrayItem.Blob -> BYTE_ARRAY
              is LexiconArrayItem.Reference -> {
                when (prop.array.items.reference) {
                  is LexiconSingleReference -> {
                    prop.array.items.reference.typeName(environment, context.document)
                  }
                  is LexiconUnionReference -> {
                    ClassName(
                      context.authority,
                      className + name.removeSuffix("s").capitalized(),
                    )
                  }
                }
              }
            }
              .let { type -> LIST.parameterizedBy(type) }
              .copy(nullable = isNullable),
          )
        }
        is LexiconObjectProperty.Primitive -> {
          SimpleProperty(
            name = name,
            type = prop.primitive.toTypeName(nullable = isNullable),
          )
        }
        is LexiconObjectProperty.Blob ->
          SimpleProperty(
            name = name,
            type = BYTE_ARRAY.copy(nullable = isNullable),
          )
        is LexiconObjectProperty.Reference -> {
          SimpleProperty(
            name = name,
            type = when (prop.reference) {
              is LexiconSingleReference -> {
                prop.reference.typeName(environment, context.document)
              }
              is LexiconUnionReference -> {
                ClassName(context.authority, className + name.capitalized())
              }
            }.copy(nullable = isNullable),
          )
        }
      }
    }

    return createDataClass(
      className = className,
      properties = properties,
    )
  }

  private fun TypeSpec.forRequestOrResponse(encoding: String): TypeSpec {
    return toBuilder()
      .addAnnotation(
        AnnotationSpec.builder(ENCODING)
          .addMember("%S", encoding)
          .build()
      )
      // Allows for custom static extension methods on the generated type.
      .addType(TypeSpec.companionObjectBuilder().build())
      .build()
  }
}
