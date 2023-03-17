package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FunSpec
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
import sh.christian.ozone.api.generator.JSON_ELEMENT
import sh.christian.ozone.api.generator.JVM_INLINE
import sh.christian.ozone.api.generator.LexiconProcessingEnvironment
import sh.christian.ozone.api.generator.SERIALIZABLE
import sh.christian.ozone.api.generator.SERIAL_NAME
import sh.christian.ozone.api.lexicon.LexiconArray
import sh.christian.ozone.api.lexicon.LexiconArrayItem
import sh.christian.ozone.api.lexicon.LexiconBlob
import sh.christian.ozone.api.lexicon.LexiconBoolean
import sh.christian.ozone.api.lexicon.LexiconDatetime
import sh.christian.ozone.api.lexicon.LexiconDocument
import sh.christian.ozone.api.lexicon.LexiconInteger
import sh.christian.ozone.api.lexicon.LexiconNumber
import sh.christian.ozone.api.lexicon.LexiconObject
import sh.christian.ozone.api.lexicon.LexiconPrimitive
import sh.christian.ozone.api.lexicon.LexiconRecord
import sh.christian.ozone.api.lexicon.LexiconSingleReference
import sh.christian.ozone.api.lexicon.LexiconString
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
  additionalConfiguration: TypeSpec.Builder.() -> Unit = {},
): TypeSpec {
  return TypeSpec.classBuilder(className)
    .addModifiers(KModifier.DATA)
    .addAnnotation(SERIALIZABLE)
    .primaryConstructor(
      FunSpec.constructorBuilder()
        .addParameters(
          properties.map { property ->
            ParameterSpec
              .builder(property.name, property.type.copy(nullable = property.nullable))
              .apply { if (property.nullable) defaultValue("null") }
              .build()
          }
        )
        .build()
    )
    .addProperties(
      properties.map { property ->
        PropertySpec
          .builder(property.name, property.type.copy(nullable = property.nullable))
          .initializer(property.name)
          .build()
      }
    )
    .apply(additionalConfiguration)
    .build()
}

fun createValueClass(
  className: ClassName,
  innerType: TypeName,
  additionalConfiguration: TypeSpec.Builder.() -> Unit = {},
): TypeSpec {
  return TypeSpec.classBuilder(className)
    .addModifiers(KModifier.VALUE)
    .addAnnotation(JVM_INLINE)
    .addAnnotation(SERIALIZABLE)
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
              AnnotationSpec.builder(SERIAL_NAME)
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
  is LexiconDatetime -> STRING
  is LexiconInteger -> LONG
  is LexiconNumber -> DOUBLE
  is LexiconString -> STRING
  is LexiconUnknown -> JSON_ELEMENT
}

fun LexiconSingleReference.typeName(
  environment: LexiconProcessingEnvironment,
  source: LexiconDocument
): TypeName {
  val lexiconRefType = environment.loadReference(source, this)
  if (lexiconRefType is LexiconString && lexiconRefType.isEnum()) {
    val refDoc = environment.loadReferenceDocument(source, this)
    return ClassName(
      refDoc.id.substringBeforeLast('.'),
      refDoc.id.substringAfterLast('.').capitalized() + "Enum",
    )
  }

  val (lexiconId, objectRef) = ref.parseLexiconRef(source)

  val packageName = lexiconId.substringBeforeLast(".")
  val className = lexiconId.substringAfterLast(".").capitalized() +
      if (objectRef == "main") "" else objectRef.capitalized()

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
  source: LexiconDocument,
  propertyName: String?,
  userType: LexiconUserType,
): TypeName = when (userType) {
  is LexiconArray -> {
    when (userType.items) {
      is LexiconArrayItem.Blob -> typeName(environment, source, propertyName, userType.items.blob)
      is LexiconArrayItem.Primitive -> typeName(
        environment,
        source,
        propertyName,
        userType.items.primitive
      )
      is LexiconArrayItem.Reference -> when (userType.items.reference) {
        is LexiconSingleReference -> userType.items.reference.typeName(environment, source)
        is LexiconUnionReference -> {
          ClassName(
            source.id.substringBeforeLast("."),
            source.id.substringAfterLast(".").capitalized() +
                propertyName!!.removeSuffix("s").capitalized(),
          )
        }
      }
    }.let { LIST.parameterizedBy(it) }
  }
  is LexiconBlob -> {
    BYTE_ARRAY
  }
  is LexiconObject -> {
    val packageName = source.id.substringBeforeLast(".")
    val className = source.id.substringAfterLast(".").capitalized() + propertyName!!.capitalized()
    ClassName(packageName, className)
  }
  is LexiconPrimitive -> {
    userType.toTypeName()
  }
  is LexiconRecord -> {
    typeName(environment, source, userType.key, userType.record)
  }
  is LexiconToken -> {
    STRING
  }
  is LexiconXrpcProcedure -> {
    val packageName = source.id.substringBeforeLast(".")
    val className = source.id.substringAfterLast(".").capitalized() + propertyName!!.capitalized()
    ClassName(packageName, className)
  }
  is LexiconXrpcQuery -> {
    val packageName = source.id.substringBeforeLast(".")
    val className = source.id.substringAfterLast(".").capitalized() + propertyName!!.capitalized()
    ClassName(packageName, className)
  }
  is LexiconXrpcSubscription -> {
    val packageName = source.id.substringBeforeLast(".")
    val className = source.id.substringAfterLast(".").capitalized() + propertyName!!.capitalized()
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
