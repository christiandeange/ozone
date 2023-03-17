package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.configurationcache.extensions.capitalized
import sh.christian.ozone.api.generator.LexiconProcessingEnvironment
import sh.christian.ozone.api.generator.SERIALIZABLE
import sh.christian.ozone.api.generator.SERIAL_NAME
import sh.christian.ozone.api.lexicon.LexiconArray
import sh.christian.ozone.api.lexicon.LexiconArrayItem
import sh.christian.ozone.api.lexicon.LexiconBlob
import sh.christian.ozone.api.lexicon.LexiconBoolean
import sh.christian.ozone.api.lexicon.LexiconDatetime
import sh.christian.ozone.api.lexicon.LexiconInteger
import sh.christian.ozone.api.lexicon.LexiconNumber
import sh.christian.ozone.api.lexicon.LexiconObject
import sh.christian.ozone.api.lexicon.LexiconObjectProperty
import sh.christian.ozone.api.lexicon.LexiconPrimitive
import sh.christian.ozone.api.lexicon.LexiconRecord
import sh.christian.ozone.api.lexicon.LexiconSingleReference
import sh.christian.ozone.api.lexicon.LexiconString
import sh.christian.ozone.api.lexicon.LexiconToken
import sh.christian.ozone.api.lexicon.LexiconUnionReference
import sh.christian.ozone.api.lexicon.LexiconUnknown
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.LexiconXrpcBody
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery
import sh.christian.ozone.api.lexicon.LexiconXrpcSchemaDefinition
import sh.christian.ozone.api.lexicon.LexiconXrpcSubscription

class LexiconDataClassesGenerator(
  private val environment: LexiconProcessingEnvironment,
) : TypesGenerator {
  override fun generateTypes(
    context: GeneratorContext,
    userType: LexiconUserType,
  ) = when (userType) {
    is LexiconArray -> generateTypes(context, userType)
    is LexiconBlob -> generateTypes(context, userType)
    is LexiconObject -> generateTypes(context, userType)
    is LexiconPrimitive -> generateTypes(context, userType)
    is LexiconRecord -> generateTypes(context, userType)
    is LexiconToken -> generateTypes(context, userType)
    is LexiconXrpcProcedure -> generateTypes(context, userType)
    is LexiconXrpcQuery -> generateTypes(context, userType)
    is LexiconXrpcSubscription -> generateTypes(context, userType)
  }

  private fun generateTypes(
    context: GeneratorContext,
    array: LexiconArray,
  ) {
    when (array.items) {
      is LexiconArrayItem.Blob -> Unit
      is LexiconArrayItem.Primitive -> Unit
      is LexiconArrayItem.Reference -> {
        when (array.items.reference) {
          is LexiconSingleReference -> Unit
          is LexiconUnionReference -> generateTypes(context, "", array.items.reference)
        }
      }
    }
  }

  private fun generateTypes(
    context: GeneratorContext,
    blob: LexiconBlob,
  ) {
    return
  }

  private fun generateTypes(
    context: GeneratorContext,
    obj: LexiconObject,
  ) {
    val properties = obj.properties.map { (propertyName, property) ->
      val nullable = propertyName !in obj.required

      val propertyType: TypeName = when (property) {
        is LexiconObjectProperty.Array -> {
          when (val itemType = property.array.items) {
            is LexiconArrayItem.Blob -> typeName(environment, context.document, "", itemType.blob)
            is LexiconArrayItem.Primitive -> typeName(
              environment,
              context.document,
              "",
              itemType.primitive
            )
            is LexiconArrayItem.Reference -> {
              when (itemType.reference) {
                is LexiconSingleReference -> {
                  itemType.reference.typeName(environment, context.document)
                }
                is LexiconUnionReference -> {
                  generateTypes(context, propertyName.removeSuffix("s"), itemType.reference)
                }
              }
            }
          }.let { LIST.parameterizedBy(it) }
        }
        is LexiconObjectProperty.Blob -> typeName(environment, context.document, "", property.blob)
        is LexiconObjectProperty.Primitive -> typeName(
          environment,
          context.document,
          "",
          property.primitive
        )
        is LexiconObjectProperty.Reference -> {
          when (property.reference) {
            is LexiconSingleReference -> property.reference.typeName(environment, context.document)
            is LexiconUnionReference -> generateTypes(context, propertyName, property.reference)
          }
        }
      }

      SimpleProperty(propertyName, propertyType, nullable)
    }

    context.addType(
      createDataClass(
        className = context.classPrefix + context.definitionName.capitalized(),
        properties = properties,
      )
    )
  }

  private fun generateTypes(
    context: GeneratorContext,
    primitive: LexiconPrimitive,
  ) {
    when (primitive) {
      is LexiconBoolean -> Unit
      is LexiconDatetime -> Unit
      is LexiconInteger -> Unit
      is LexiconNumber -> Unit
      is LexiconString -> {
        if (primitive.knownValues.isNotEmpty()) {
          context.addTypeAlias(TypeAliasSpec.builder(context.classPrefix, STRING).build())
        }
      }
      is LexiconUnknown -> Unit
    }
  }

  private fun generateTypes(
    context: GeneratorContext,
    record: LexiconRecord,
  ) {
    generateTypes(context, record.record)
  }

  private fun generateTypes(
    context: GeneratorContext,
    token: LexiconToken,
  ) {
    val className = ClassName(context.authority, context.classPrefix + "Enum")

    val enumName =
      context.definitionName.takeIf { it.isNotBlank() }
        ?: context.procedureName

    context.addEnum(className, enumName)
  }

  private fun generateTypes(
    context: GeneratorContext,
    xrpcProcedure: LexiconXrpcProcedure,
  ) {
    xrpcProcedure.input?.let { generateTypes(context, "Request", it) }
    xrpcProcedure.output?.let { generateTypes(context, "Response", it) }
  }

  private fun generateTypes(
    context: GeneratorContext,
    xrpcQuery: LexiconXrpcQuery,
  ) {
    xrpcQuery.output?.let { generateTypes(context, "Response", it) }
  }

  private fun generateTypes(
    context: GeneratorContext,
    xrpcSubscription: LexiconXrpcSubscription,
  ) {
    // TODO
  }

  private fun generateTypes(
    context: GeneratorContext,
    className: String,
    xrpcProcedure: LexiconXrpcBody,
  ) {
    val schema = xrpcProcedure.schema ?: return
    when (schema) {
      is LexiconXrpcSchemaDefinition.Object -> {
        schema.value.properties.forEach { (propertyName, property) ->
          when (property) {
            is LexiconObjectProperty.Array -> {
              when (property.array.items) {
                is LexiconArrayItem.Blob -> Unit
                is LexiconArrayItem.Primitive -> {
                  generateTypes(context, property.array.items.primitive)
                }
                is LexiconArrayItem.Reference -> {
                  when (property.array.items.reference) {
                    is LexiconSingleReference -> Unit
                    is LexiconUnionReference -> {
                      generateTypes(
                        context,
                        className + propertyName.removeSuffix("s").capitalized(),
                        property.array.items.reference
                      )
                    }
                  }
                }
              }
            }
            is LexiconObjectProperty.Blob -> Unit
            is LexiconObjectProperty.Primitive -> {
              generateTypes(context, property.primitive)
            }
            is LexiconObjectProperty.Reference -> {
              when (property.reference) {
                is LexiconSingleReference -> Unit
                is LexiconUnionReference -> {
                  generateTypes(
                    context,
                    className + propertyName.removeSuffix("s").capitalized(),
                    property.reference
                  )
                }
              }
            }
          }
        }
      }
      is LexiconXrpcSchemaDefinition.Reference -> {
        when (schema.reference) {
          is LexiconSingleReference -> Unit
          is LexiconUnionReference -> {
            generateTypes(context, className, schema.reference)
          }
        }
      }
    }
  }

  private fun generateTypes(
    context: GeneratorContext,
    className: String,
    unionReference: LexiconUnionReference,
  ): TypeName {
    val classSimpleName = context.classPrefix +
        context.definitionName.capitalized() +
        className.capitalized() +
        "Union"

    val name = ClassName(context.authority, classSimpleName)

    val sealedInterface = TypeSpec.interfaceBuilder(name)
      .addModifiers(KModifier.SEALED)
      .addAnnotation(SERIALIZABLE)
      .build()

    context.addType(sealedInterface)

    unionReference.references.forEachIndexed { i, reference ->
      val typeName = reference.typeName(environment, context.document)

      context.addType(
        createValueClass(
          className = ClassName(context.authority, name.simpleName + (i + 1)),
          innerType = typeName,
          additionalConfiguration = {
            addSuperinterface(name)

            val (lexiconId, objectRef) = reference.ref.parseLexiconRef(context.document)
            addAnnotation(
              AnnotationSpec.builder(SERIAL_NAME)
                .addMember("%S", "$lexiconId#$objectRef")
                .build()
            )
          }
        ),
      )
    }

    return name
  }
}
