package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.configurationcache.extensions.capitalized
import sh.christian.ozone.api.generator.LexiconProcessingEnvironment
import sh.christian.ozone.api.generator.TypeNames
import sh.christian.ozone.api.generator.valueClassSerializer
import sh.christian.ozone.api.lexicon.LexiconArray
import sh.christian.ozone.api.lexicon.LexiconArrayItem
import sh.christian.ozone.api.lexicon.LexiconBlob
import sh.christian.ozone.api.lexicon.LexiconBoolean
import sh.christian.ozone.api.lexicon.LexiconBytes
import sh.christian.ozone.api.lexicon.LexiconCidLink
import sh.christian.ozone.api.lexicon.LexiconDocument
import sh.christian.ozone.api.lexicon.LexiconFloat
import sh.christian.ozone.api.lexicon.LexiconInteger
import sh.christian.ozone.api.lexicon.LexiconIpldType
import sh.christian.ozone.api.lexicon.LexiconObject
import sh.christian.ozone.api.lexicon.LexiconPrimitive
import sh.christian.ozone.api.lexicon.LexiconRecord
import sh.christian.ozone.api.lexicon.LexiconSingleReference
import sh.christian.ozone.api.lexicon.LexiconString
import sh.christian.ozone.api.lexicon.LexiconStringFormat
import sh.christian.ozone.api.lexicon.LexiconToken
import sh.christian.ozone.api.lexicon.LexiconUnionReference
import sh.christian.ozone.api.lexicon.LexiconUnknown
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery
import sh.christian.ozone.api.lexicon.LexiconXrpcSubscription

fun createDataClass(
  className: String,
  properties: List<SimpleProperty>,
  description: String?,
): TypeSpec {
  return TypeSpec.classBuilder(className)
    .addModifiers(KModifier.DATA)
    .addAnnotation(TypeNames.Serializable)
    .primaryConstructor(
      FunSpec.constructorBuilder()
        .addParameters(
          properties.map { property ->
            ParameterSpec
              .builder(
                property.name,
                property.type.copy(
                  nullable = property.nullable && property.type !is ParameterizedTypeName
                )
              )
              .apply {
                if (property.nullable) {
                  defaultValue(property.defaultValue())
                }
                if (property.description != null) {
                  addKdoc(property.description)
                }
                if ((property.type as? ParameterizedTypeName)?.rawType == TypeNames.ImmutableList) {
                  addAnnotation(
                    AnnotationSpec.builder(TypeNames.Serializable)
                      .addMember("%T::class", TypeNames.ImmutableListSerializer)
                      .build()
                  )
                }
              }
              .build()
          }
        )
        .build()
    )
    .addProperties(
      properties.map { property ->
        PropertySpec
          .builder(
            property.name,
            property.type.copy(
              nullable = property.nullable && property.type !is ParameterizedTypeName
            )
          )
          .initializer(property.name)
          .build()
      }
    )
    .apply {
      if (description != null) {
        addKdoc(description)
      }
    }
    .build()
}

fun createValueClass(
  className: ClassName,
  innerType: TypeName,
  additionalConfiguration: TypeSpec.Builder.() -> Unit = {},
): List<TypeSpec> {
  val serializerClassName = className.peerClass(className.simpleName + "Serializer")
  val serializerTypeSpec = TypeSpec.classBuilder(serializerClassName)
    .addSuperinterface(
      TypeNames.KSerializer.parameterizedBy(className),
      CodeBlock.of("%M()", valueClassSerializer)
    )
    .build()

  val valueClassTypeSpec = TypeSpec.classBuilder(className)
    .addAnnotation(
      AnnotationSpec.builder(TypeNames.Serializable)
        .addMember("with = %T::class", serializerClassName)
        .build()
    )
    .addModifiers(KModifier.VALUE)
    .addAnnotation(JvmInline::class)
    .primaryConstructor(
      FunSpec.constructorBuilder()
        .addParameter(
          ParameterSpec
            .builder("value", innerType)
            .build()
        )
        .build()
    )
    .addProperty(
      PropertySpec
        .builder("value", innerType)
        .initializer("value")
        .build()
    )
    .apply(additionalConfiguration)
    .build()

  return listOf(serializerTypeSpec, valueClassTypeSpec)
}

fun createEnumClass(
  className: ClassName,
  values: Collection<String>,
  additionalConfiguration: TypeSpec.Builder.() -> Unit = {},
): TypeSpec {
  return TypeSpec.enumBuilder(className)
    .apply {
      values.forEach { value ->
        addEnumConstant(
          name = value.toEnumCase(),
          typeSpec = TypeSpec.anonymousClassBuilder()
            .addAnnotation(
              AnnotationSpec.builder(TypeNames.SerialName)
                .addMember("%S", value)
                .build()
            )
            .build(),
        )
      }
    }
    .apply(additionalConfiguration)
    .build()
}

fun LexiconPrimitive.toTypeName() = when (this) {
  is LexiconBoolean -> BOOLEAN
  is LexiconInteger -> LONG
  is LexiconFloat -> DOUBLE
  is LexiconString -> {
    when (format) {
      LexiconStringFormat.DATETIME -> TypeNames.Instant
      LexiconStringFormat.URI -> TypeNames.Uri
      LexiconStringFormat.AT_URI -> TypeNames.AtUri
      LexiconStringFormat.DID -> TypeNames.Did
      LexiconStringFormat.HANDLE -> TypeNames.Handle
      LexiconStringFormat.AT_IDENTIFIER -> TypeNames.AtIdentifier
      LexiconStringFormat.NSID -> TypeNames.Nsid
      LexiconStringFormat.CID -> TypeNames.Cid
      LexiconStringFormat.LANGUAGE -> TypeNames.Language
      null -> STRING
    }
  }
  is LexiconUnknown -> TypeNames.JsonElement
}

fun LexiconSingleReference.typeName(
  environment: LexiconProcessingEnvironment,
  source: LexiconDocument
): TypeName {
  val lexiconRefType = environment.loadReference(source, this)
  if (lexiconRefType is LexiconString && lexiconRefType.isEnum()) {
    val refDoc = environment.loadReferenceDocument(source, this)
    return ClassName(
      refDoc.id.substringBeforeLast(".").removeSuffix(".defs"),
      refDoc.id.substringAfterLast(".").removePrefix("defs").capitalized() + "Token",
    )
  }

  val isUnionType = when (lexiconRefType) {
    is LexiconArray -> {
      when (lexiconRefType.items) {
        is LexiconArrayItem.Reference -> {
          lexiconRefType.items.reference is LexiconUnionReference
        }
        is LexiconArrayItem.Blob,
        is LexiconArrayItem.IpldType,
        is LexiconArrayItem.Primitive -> false
      }
    }
    is LexiconBlob,
    is LexiconIpldType,
    is LexiconObject,
    is LexiconPrimitive,
    is LexiconRecord,
    is LexiconToken,
    is LexiconXrpcQuery,
    is LexiconXrpcProcedure,
    is LexiconXrpcSubscription -> false
  }

  val (lexiconId, objectRef) = ref.parseLexiconRef(source)

  val packageName = lexiconId.substringBeforeLast(".").removeSuffix(".defs")
  val className = lexiconId.substringAfterLast(".").removePrefix("defs").capitalized() +
      if (objectRef == "main") "" else objectRef.capitalized() +
          if (isUnionType) "Union" else ""

  return ClassName(packageName, className)
}

fun String.parseLexiconRef(source: LexiconDocument): Pair<String, String> {
  return if ('#' !in this) {
    this to "main"
  } else if (startsWith('#')) {
    source.id to this.drop(1)
  } else {
    val (lexiconId, objectRef) = split('#')
    lexiconId to objectRef
  }
}

fun typeName(
  environment: LexiconProcessingEnvironment,
  context: GeneratorContext,
  propertyName: String?,
  userType: LexiconUserType,
): TypeName = when (userType) {
  is LexiconArray -> {
    when (userType.items) {
      is LexiconArrayItem.Blob -> typeName(environment, context, propertyName, userType.items.blob)
      is LexiconArrayItem.IpldType -> typeName(
        environment,
        context,
        propertyName,
        userType.items.ipld,
      )
      is LexiconArrayItem.Primitive -> typeName(
        environment,
        context,
        propertyName,
        userType.items.primitive,
      )
      is LexiconArrayItem.Reference -> when (userType.items.reference) {
        is LexiconSingleReference -> {
          userType.items.reference.typeName(environment, context.document)
        }
        is LexiconUnionReference -> {
          val sourceId = context.document.id
          ClassName(
            sourceId.substringBeforeLast("."),
            sourceId.substringAfterLast(".").capitalized() +
                propertyName!!.removeSuffix("s").capitalized(),
          )
        }
      }
    }.let { TypeNames.ImmutableList.parameterizedBy(it) }
  }
  is LexiconBlob -> {
    TypeNames.JsonElement
  }
  is LexiconIpldType -> {
    when (userType) {
      is LexiconBytes -> BYTE_ARRAY
      is LexiconCidLink -> STRING
    }
  }
  is LexiconObject -> {
    val sourceId = context.document.id
    val packageName = sourceId.substringBeforeLast(".")
    val className = sourceId.substringAfterLast(".").capitalized() + propertyName!!.capitalized()
    ClassName(packageName, className)
  }
  is LexiconPrimitive -> {
    userType.toTypeName()
  }
  is LexiconRecord -> {
    typeName(environment, context, userType.key, userType.record)
  }
  is LexiconToken -> {
    STRING
  }
  is LexiconXrpcProcedure -> {
    val sourceId = context.document.id
    val packageName = sourceId.substringBeforeLast(".")
    val className = sourceId.substringAfterLast(".").capitalized() + propertyName!!.capitalized()
    ClassName(packageName, className)
  }
  is LexiconXrpcQuery -> {
    val sourceId = context.document.id
    val packageName = sourceId.substringBeforeLast(".")
    val className = sourceId.substringAfterLast(".").capitalized() + propertyName!!.capitalized()
    ClassName(packageName, className)
  }
  is LexiconXrpcSubscription -> {
    val sourceId = context.document.id
    val packageName = sourceId.substringBeforeLast(".")
    val className = sourceId.substringAfterLast(".").capitalized() + propertyName!!.capitalized()
    ClassName(packageName, className)
  }
}

fun LexiconString.isEnum(): Boolean {
  return knownValues.isNotEmpty() && knownValues.all { '#' in it }
}

private val CAMEL_CASE_REGEX = "(?<=[a-zA-Z])[A-Z]".toRegex()

private fun String.toSnakeCase(): String {
  return CAMEL_CASE_REGEX.replace(this) { "_${it.value}" }.lowercase()
}

private fun String.toEnumCase(): String {
  return CAMEL_CASE_REGEX.replace(this) { "_${it.value}" }.uppercase()
}
