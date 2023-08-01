package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.configurationcache.extensions.capitalized
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
import sh.christian.ozone.api.lexicon.LexiconToken
import sh.christian.ozone.api.lexicon.LexiconUnionReference
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.LexiconXrpcBody
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
  ) = when (userType) {
    is LexiconArray -> generateTypes(context, userType)
    is LexiconBlob -> generateTypes(context, userType)
    is LexiconIpldType -> generateTypes(context, userType)
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
      is LexiconArrayItem.IpldType -> Unit
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
    ipldType: LexiconIpldType,
  ) {
    return
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
            is LexiconArrayItem.Primitive -> typeName(
              environment,
              context,
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
          }.let { TypeNames.ImmutableList.parameterizedBy(it) }
        }
        is LexiconObjectProperty.Blob -> typeName(environment, context, "", property.blob)
        is LexiconObjectProperty.IpldType -> typeName(environment, context, "", property.ipld)
        is LexiconObjectProperty.Primitive -> typeName(
          environment,
          context,
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

      val description = when (property) {
        is LexiconObjectProperty.Array -> property.array.description
        is LexiconObjectProperty.Blob -> property.blob.description
        is LexiconObjectProperty.IpldType -> property.ipld.description
        is LexiconObjectProperty.Primitive -> property.primitive.description
        is LexiconObjectProperty.Reference -> property.reference.description
      }

      SimpleProperty(propertyName, propertyType, nullable, description)
    }

    context.addType(
      createDataClass(
        className = context.classPrefix + context.definitionName.capitalized(),
        properties = properties,
        description = obj.description,
      )
    )
  }

  private fun generateTypes(
    context: GeneratorContext,
    primitive: LexiconPrimitive,
  ) {
    return
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
    xrpcSubscription.message?.let { generateTypes(context, "Message", it) }
  }

  private fun generateTypes(
    context: GeneratorContext,
    className: String,
    xrpcBody: LexiconXrpcBody,
  ) {
    xrpcBody.schema?.let { schema ->
      generateTypes(context, className, schema)
    }
  }

  private fun generateTypes(
    context: GeneratorContext,
    className: String,
    xrpcSubscriptionMessage: LexiconXrpcSubscriptionMessage,
  ) {
    xrpcSubscriptionMessage.schema?.let { schema ->
      generateTypes(context, className, schema)
    }
  }

  private fun generateTypes(
    context: GeneratorContext,
    className: String,
    schema: LexiconXrpcSchemaDefinition,
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
            is LexiconObjectProperty.IpldType -> Unit
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
      .addAnnotation(TypeNames.Serializable)

    val canonicalReferences = unionReference.references
      .map { it.ref.parseLexiconRef(context.document) }
      .map { (lexiconId, objectRef) -> "$lexiconId#$objectRef" }

    val (commonPrefix, commonSuffix) =
      canonicalReferences.commonPrefix() to canonicalReferences.commonSuffix()

    unionReference.references.forEach { reference ->
      val typeName = reference.typeName(environment, context.document)
      val uniqueName = reference.ref
        .removePrefix(commonPrefix)
        .removeSuffix(commonSuffix)
        .replace(".defs", "")
        .replace(Regex("[.#][a-z]")) { it.value[1].uppercase() }
        .capitalized()

      sealedInterface.addTypes(
        createValueClass(
          className = name.nestedClass(uniqueName),
          innerType = typeName,
          additionalConfiguration = {
            addSuperinterface(name)

            val (lexiconId, objectRef) = reference.ref.parseLexiconRef(context.document)
            addAnnotation(
              AnnotationSpec.builder(TypeNames.SerialName)
                .addMember("%S", "$lexiconId#$objectRef".removeSuffix("#main"))
                .build()
            )
          }
        ),
      )
    }

    context.addType(sealedInterface.build())

    return name
  }
}

private fun List<String>.commonPrefix(): String = when (size) {
  0 -> ""
  1 -> first()
  else -> {
    val sample = first()
    sample.forEachIndexed { i, c ->
      if (any { it.count() <= i || it[i] != c }) {
        return sample.substring(0, i)
      }
    }
    sample
  }
}

private fun List<String>.commonSuffix(): String = when (size) {
  0 -> ""
  1 -> first()
  else -> {
    val sample = first().reversed()
    sample.forEachIndexed { i, c ->
      if (any { it.count() <= i || it[i] != c }) {
        return sample.substring(0, i).reversed()
      }
    }
    first()
  }
}
