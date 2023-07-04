package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.configurationcache.extensions.capitalized
import sh.christian.ozone.api.generator.LexiconProcessingEnvironment
import sh.christian.ozone.api.generator.TypeNames
import sh.christian.ozone.api.lexicon.LexiconArrayItem
import sh.christian.ozone.api.lexicon.LexiconBytes
import sh.christian.ozone.api.lexicon.LexiconCidLink
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
      else -> Unit
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
        context.addType(createType(context, className, body.schema.value))
      }
      is LexiconXrpcSchemaDefinition.Reference -> {
        val typeAliasType = when (body.schema.reference) {
          is LexiconSingleReference -> body.schema.reference.typeName(environment, context.document)
          is LexiconUnionReference -> ClassName(
            context.authority, className.capitalized() + "Union"
          )
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
      val nullable = name !in body.required

      when (prop) {
        is LexiconObjectProperty.Array -> {
          SimpleProperty(
            name = name,
            nullable = nullable,
            type = when (prop.array.items) {
              is LexiconArrayItem.Primitive -> {
                prop.array.items.primitive.toTypeName()
              }
              is LexiconArrayItem.Blob -> TypeNames.JsonElement
              is LexiconArrayItem.IpldType -> {
                when (prop.array.items.ipld) {
                  is LexiconBytes -> BYTE_ARRAY
                  is LexiconCidLink -> STRING
                }
              }
              is LexiconArrayItem.Reference -> {
                when (prop.array.items.reference) {
                  is LexiconSingleReference -> {
                    prop.array.items.reference.typeName(environment, context.document)
                  }
                  is LexiconUnionReference -> {
                    ClassName(
                      context.authority,
                      className + name.removeSuffix("s").capitalized() + "Union",
                    )
                  }
                }
              }
            }.let { type -> TypeNames.ImmutableList.parameterizedBy(type) },
            description = when (val items = prop.array.items) {
              is LexiconArrayItem.Blob -> items.blob.description
              is LexiconArrayItem.IpldType -> items.ipld.description
              is LexiconArrayItem.Primitive -> items.primitive.description
              is LexiconArrayItem.Reference -> items.reference.description
            },
          )
        }
        is LexiconObjectProperty.Primitive -> {
          SimpleProperty(
            name = name,
            type = prop.primitive.toTypeName(),
            nullable = nullable,
            description = prop.primitive.description,
          )
        }
        is LexiconObjectProperty.Blob ->
          SimpleProperty(
            name = name,
            type = TypeNames.JsonElement,
            nullable = nullable,
            description = prop.blob.description,
          )
        is LexiconObjectProperty.IpldType ->
          SimpleProperty(
            name = name,
            nullable = nullable,
            type = when (prop.ipld) {
              is LexiconBytes -> BYTE_ARRAY
              is LexiconCidLink -> STRING
            },
            description = prop.ipld.description,
          )
        is LexiconObjectProperty.Reference -> {
          SimpleProperty(
            name = name,
            nullable = nullable,
            type = when (prop.reference) {
              is LexiconSingleReference -> {
                prop.reference.typeName(environment, context.document)
              }
              is LexiconUnionReference -> {
                ClassName(context.authority, className + name.capitalized() + "Union")
              }
            },
            description = prop.reference.description,
          )
        }
      }
    }

    return createDataClass(
      className = className,
      properties = properties,
      description = body.description,
    )
  }
}
