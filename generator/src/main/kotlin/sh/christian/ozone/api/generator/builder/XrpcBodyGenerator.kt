package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.configurationcache.extensions.capitalized
import sh.christian.ozone.api.generator.LexiconProcessingEnvironment
import sh.christian.ozone.api.generator.TypeNames
import sh.christian.ozone.api.lexicon.LexiconArrayItem
import sh.christian.ozone.api.lexicon.LexiconObject
import sh.christian.ozone.api.lexicon.LexiconObjectProperty
import sh.christian.ozone.api.lexicon.LexiconSingleReference
import sh.christian.ozone.api.lexicon.LexiconUnionReference
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery
import sh.christian.ozone.api.lexicon.LexiconXrpcSchemaDefinition
import sh.christian.ozone.api.lexicon.LexiconXrpcSubscription

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
          createBodyType(context, "${context.classPrefix}Request", it.schema)
        }
        userType.output?.let {
          createBodyType(context, "${context.classPrefix}Response", it.schema)
        }
      }
      is LexiconXrpcQuery -> {
        userType.output?.let {
          createBodyType(context, "${context.classPrefix}Response", it.schema)
        }
      }
      is LexiconXrpcSubscription -> {
        userType.message?.let {
          createBodyType(context, "${context.classPrefix}Message", it.schema)
        }
      }
      else -> Unit
    }
  }

  private fun createBodyType(
    context: GeneratorContext,
    className: String,
    schema: LexiconXrpcSchemaDefinition?,
  ) {
    when (schema) {
      null -> Unit
      is LexiconXrpcSchemaDefinition.Object -> {
        context.addType(createType(context, className, schema.value))
      }
      is LexiconXrpcSchemaDefinition.Reference -> {
        val typeAliasType = when (schema.reference) {
          is LexiconSingleReference -> schema.reference.typeName(environment, context.document)
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
                context.primitiveTypeName(prop.array.items.primitive, name)
              }
              is LexiconArrayItem.Blob -> TypeNames.JsonElement
              is LexiconArrayItem.IpldType -> BYTE_ARRAY
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
            }.let { type -> TypeNames.ReadOnlyList.parameterizedBy(type) },
            description = when (val items = prop.array.items) {
              is LexiconArrayItem.Blob -> items.blob.description
              is LexiconArrayItem.IpldType -> items.ipld.description
              is LexiconArrayItem.Primitive -> items.primitive.description
              is LexiconArrayItem.Reference -> items.reference.description
            },
            defaultValue = when (val items = prop.array.items) {
              is LexiconArrayItem.Primitive -> context.primitiveDefaultValue(items.primitive, name)
              is LexiconArrayItem.Blob,
              is LexiconArrayItem.IpldType,
              is LexiconArrayItem.Reference -> null
            }
          )
        }
        is LexiconObjectProperty.Primitive -> {
          SimpleProperty(
            name = name,
            type = context.primitiveTypeName(prop.primitive, name),
            nullable = nullable,
            description = prop.primitive.description,
            defaultValue = context.primitiveDefaultValue(prop.primitive, name),
          )
        }
        is LexiconObjectProperty.Blob ->
          SimpleProperty(
            name = name,
            type = TypeNames.JsonElement,
            nullable = nullable,
            description = prop.blob.description,
            defaultValue = null,
          )
        is LexiconObjectProperty.IpldType ->
          SimpleProperty(
            name = name,
            nullable = nullable,
            type = BYTE_ARRAY,
            description = prop.ipld.description,
            defaultValue = null,
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
            defaultValue = null,
          )
        }
      }
    }

    return createClassForProperties(
      className = className,
      properties = properties,
      description = body.description,
    )
  }
}
