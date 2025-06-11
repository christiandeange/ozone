package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import sh.christian.ozone.api.generator.LexiconProcessingEnvironment
import sh.christian.ozone.api.generator.TypeNames
import sh.christian.ozone.api.lexicon.LexiconArray
import sh.christian.ozone.api.lexicon.LexiconArrayItem
import sh.christian.ozone.api.lexicon.LexiconBlob
import sh.christian.ozone.api.lexicon.LexiconIpldType
import sh.christian.ozone.api.lexicon.LexiconObject
import sh.christian.ozone.api.lexicon.LexiconObjectProperty
import sh.christian.ozone.api.lexicon.LexiconPrimitive
import sh.christian.ozone.api.lexicon.LexiconRecord
import sh.christian.ozone.api.lexicon.LexiconSingleReference
import sh.christian.ozone.api.lexicon.LexiconString
import sh.christian.ozone.api.lexicon.LexiconToken
import sh.christian.ozone.api.lexicon.LexiconUnionReference
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.LexiconXrpcBody
import sh.christian.ozone.api.lexicon.LexiconXrpcParameter
import sh.christian.ozone.api.lexicon.LexiconXrpcParameters
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery
import sh.christian.ozone.api.lexicon.LexiconXrpcSchemaDefinition
import sh.christian.ozone.api.lexicon.LexiconXrpcSubscription
import sh.christian.ozone.api.lexicon.LexiconXrpcSubscriptionMessage

class LexiconDataClassesGenerator(
  private val environment: LexiconProcessingEnvironment,
) : TypesGenerator {
  override fun generateTypes(
    context: GeneratorContext,
    userType: LexiconUserType,
  ) {
    when (userType) {
      is LexiconArray -> generateTypes(context, userType)
      is LexiconBlob -> return
      is LexiconIpldType -> return
      is LexiconObject -> generateTypes(context, userType)
      is LexiconPrimitive -> generateTypes(context, userType)
      is LexiconRecord -> generateTypes(context, userType)
      is LexiconToken -> generateTypes(context, userType)
      is LexiconXrpcProcedure -> generateTypes(context, userType)
      is LexiconXrpcQuery -> generateTypes(context, userType)
      is LexiconXrpcSubscription -> generateTypes(context, userType)
    }
  }

  private fun generateTypes(
    context: GeneratorContext,
    array: LexiconArray,
  ) {
    when (array.items) {
      is LexiconArrayItem.Blob -> Unit
      is LexiconArrayItem.IpldType -> Unit
      is LexiconArrayItem.Primitive -> generateTypes(context, array.items.primitive)
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
    obj: LexiconObject,
  ) {
    val properties = obj.properties.map { (propertyName, property) ->
      val nullable = propertyName !in obj.required || propertyName in obj.nullable

      val propertyType: TypeName = when (property) {
        is LexiconObjectProperty.Array -> {
          when (val itemType = property.array.items) {
            is LexiconArrayItem.Blob -> typeName(environment, context, "", itemType.blob)
            is LexiconArrayItem.IpldType -> typeName(
              environment,
              context,
              "",
              itemType.ipld
            )
            is LexiconArrayItem.Primitive -> generateTypes(context, itemType.primitive, propertyName)
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
        is LexiconObjectProperty.Blob -> typeName(environment, context, "", property.blob)
        is LexiconObjectProperty.IpldType -> typeName(environment, context, "", property.ipld)
        is LexiconObjectProperty.Primitive -> generateTypes(context, property.primitive, propertyName)
        is LexiconObjectProperty.Reference -> {
          when (property.reference) {
            is LexiconSingleReference -> property.reference.typeName(environment, context.document)
            is LexiconUnionReference -> generateTypes(context, propertyName, property.reference)
          }
        }
      }

      val description = when (property) {
        is LexiconObjectProperty.Array -> property.array.description
        is LexiconObjectProperty.Blob -> property.blob.description
        is LexiconObjectProperty.IpldType -> property.ipld.description
        is LexiconObjectProperty.Primitive -> property.primitive.description
        is LexiconObjectProperty.Reference -> property.reference.description
      }

      val defaultValue = when (property) {
        is LexiconObjectProperty.Array -> {
          when (val items = property.array.items) {
            is LexiconArrayItem.Primitive -> context.primitiveDefaultValue(items.primitive, propertyName)
            is LexiconArrayItem.Blob,
            is LexiconArrayItem.IpldType,
            is LexiconArrayItem.Reference -> null
          }
        }
        is LexiconObjectProperty.Primitive -> context.primitiveDefaultValue(property.primitive, propertyName)
        is LexiconObjectProperty.Blob,
        is LexiconObjectProperty.IpldType,
        is LexiconObjectProperty.Reference -> null
      }
      val requirements = property.requirements()

      SimpleProperty(propertyName, propertyType, nullable, description, defaultValue, requirements)
    }

    context.addType(
      createClassForProperties(
        className = ClassName(context.authority, context.classPrefix + context.definitionName.capitalized()),
        properties = properties,
        description = obj.description,
      )
    )
  }

  private fun generateTypes(
    context: GeneratorContext,
    primitive: LexiconPrimitive,
    propertyName: String = "",
  ): TypeName {
    if (primitive !is LexiconString || !primitive.isEnumValues()) {
      return typeName(environment, context, "", primitive)
    }

    val simpleClassName = if (propertyName.isNotEmpty()) {
      context.classPrefix + propertyName.capitalized()
    } else {
      context.classPrefix + context.definitionName.capitalized()
    }

    val className = ClassName(context.authority, simpleClassName)
    primitive.knownValues.forEach { enum ->
      context.addEnum(className, primitive.description, enum, null)
    }
    return className
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
    val className = ClassName(context.authority, context.classPrefix + "Token")
    val enumName = context.document.id + "#" + context.definitionName
    context.addEnum(className, null, enumName, token.description)
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
    xrpcQuery.parameters?.let { generateTypes(context, it) }
    xrpcQuery.output?.let { generateTypes(context, "Response", it) }
  }

  private fun generateTypes(
    context: GeneratorContext,
    xrpcSubscription: LexiconXrpcSubscription,
  ) {
    xrpcSubscription.message?.let { generateTypes(context, it) }
  }

  private fun generateTypes(
    context: GeneratorContext,
    xrpcBody: LexiconXrpcParameters,
  ) {
    xrpcBody.properties.forEach { (name, property) ->
      when (property) {
        is LexiconXrpcParameter.Primitive -> generateTypes(context, property.primitive, name)
        is LexiconXrpcParameter.PrimitiveArray -> generateTypes(context, property.array.items, name)
      }
    }
  }

  private fun generateTypes(
    context: GeneratorContext,
    className: String,
    xrpcBody: LexiconXrpcBody,
  ) {
    xrpcBody.schema?.let { schema ->
      generateTypes(context, className, schema, addRelationships = false)
    }
  }

  private fun generateTypes(
    context: GeneratorContext,
    xrpcSubscriptionMessage: LexiconXrpcSubscriptionMessage,
  ) {
    xrpcSubscriptionMessage.schema?.let { schema ->
      generateTypes(context, "Message", schema, addRelationships = true)
    }
  }

  private fun generateTypes(
    context: GeneratorContext,
    className: String,
    schema: LexiconXrpcSchemaDefinition,
    addRelationships: Boolean,
  ) {
    when (schema) {
      is LexiconXrpcSchemaDefinition.Object -> {
        schema.value.properties.forEach { (propertyName, property) ->
          when (property) {
            is LexiconObjectProperty.Array -> {
              when (property.array.items) {
                is LexiconArrayItem.Blob -> Unit
                is LexiconArrayItem.IpldType -> Unit
                is LexiconArrayItem.Primitive -> {
                  generateTypes(context, property.array.items.primitive, propertyName)
                }
                is LexiconArrayItem.Reference -> {
                  when (property.array.items.reference) {
                    is LexiconSingleReference -> Unit
                    is LexiconUnionReference -> {
                      generateTypes(
                        context,
                        className + propertyName.removeSuffix("s").capitalized(),
                        property.array.items.reference,
                        addRelationships,
                      )
                    }
                  }
                }
              }
            }
            is LexiconObjectProperty.Blob -> Unit
            is LexiconObjectProperty.IpldType -> Unit
            is LexiconObjectProperty.Primitive -> {
              generateTypes(context, property.primitive, propertyName)
            }
            is LexiconObjectProperty.Reference -> {
              when (property.reference) {
                is LexiconSingleReference -> Unit
                is LexiconUnionReference -> {
                  generateTypes(
                    context,
                    className + propertyName.removeSuffix("s").capitalized(),
                    property.reference,
                    addRelationships,
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
            generateTypes(context, className, schema.reference, addRelationships)
          }
        }
      }
    }
  }

  private fun generateTypes(
    context: GeneratorContext,
    className: String,
    unionReference: LexiconUnionReference,
    addRelationships: Boolean = false,
  ): TypeName {
    val classSimpleName = context.classPrefix +
        context.definitionName.capitalized() +
        className.capitalized() +
        "Union"

    val name = ClassName(context.authority, classSimpleName)

    val sealedInterface = TypeSpec.interfaceBuilder(name)
      .addModifiers(KModifier.SEALED)
      .addAnnotation(TypeNames.Serializable)

    val canonicalReferences = unionReference.references
      .map { it.ref.parseLexiconRef(context.document) }
      .map { (lexiconId, objectRef) -> "$lexiconId#$objectRef" }

    val (commonPrefix, commonSuffix) = if (canonicalReferences.size == 1) {
      canonicalReferences.first().substringBefore('#') + '#' to ""
      // Special case for when there's only one value in the union. Strip out the authority prefix, if present.
    } else {
      canonicalReferences.commonPrefix() to canonicalReferences.commonSuffix()
    }

    unionReference.references.forEach { reference ->
      val typeName = reference.typeName(environment, context.document)
      val uniqueName = reference.ref
        .removePrefix(commonPrefix)
        .removeSuffix(commonSuffix)
        .replace(".defs", "")
        .replace(Regex("[.#][a-z]")) { it.value[1].uppercase() }
        .capitalized()

      val (lexiconId, objectRef) = reference.ref.parseLexiconRef(context.document)
      val serialName = "$lexiconId#$objectRef".removeSuffix("#main")

      val childClassName = name.nestedClass(uniqueName)
      sealedInterface.addTypes(
        createValueClass(
          className = childClassName,
          innerType = typeName,
          serialName = serialName,
          additionalConfiguration = {
            addSuperinterface(name)
          }
        ),
      )

      if (addRelationships) {
        context.addSealedRelationship(name, childClassName, serialName)
      }
    }

    if (environment.defaults.generateUnknownsForSealedTypes) {
      sealedInterface.addTypes(
        createValueClass(
          className = name.nestedClass("Unknown"),
          innerType = TypeNames.JsonContent,
          serialName = null,
          additionalConfiguration = {
            addSuperinterface(name)
          }
        ),
      )
    }

    context.addType(sealedInterface.build())

    return name
  }
}
