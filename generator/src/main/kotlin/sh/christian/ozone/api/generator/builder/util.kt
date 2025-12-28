package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.Annotatable
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.Documentable
import com.squareup.kotlinpoet.Dynamic
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import sh.christian.ozone.api.generator.LexiconProcessingEnvironment
import sh.christian.ozone.api.generator.TypeNames
import sh.christian.ozone.api.generator.className
import sh.christian.ozone.api.generator.stringEnumSerializer
import sh.christian.ozone.api.lexicon.LexiconArray
import sh.christian.ozone.api.lexicon.LexiconArrayItem
import sh.christian.ozone.api.lexicon.LexiconBlob
import sh.christian.ozone.api.lexicon.LexiconDocument
import sh.christian.ozone.api.lexicon.LexiconIpldType
import sh.christian.ozone.api.lexicon.LexiconObject
import sh.christian.ozone.api.lexicon.LexiconPrimitive
import sh.christian.ozone.api.lexicon.LexiconRecord
import sh.christian.ozone.api.lexicon.LexiconSingleReference
import sh.christian.ozone.api.lexicon.LexiconString
import sh.christian.ozone.api.lexicon.LexiconToken
import sh.christian.ozone.api.lexicon.LexiconUnionReference
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery
import sh.christian.ozone.api.lexicon.LexiconXrpcSubscription

fun createClassForProperties(
  className: ClassName,
  properties: List<SimpleProperty>,
  description: String?,
): TypeSpec {
  return if (properties.isEmpty()) {
    createObjectClass(className, description)
  } else {
    createDataClass(className, properties, description)
  }
}

fun createDataClass(
  className: ClassName,
  properties: List<SimpleProperty>,
  description: String?,
): TypeSpec {
  return TypeSpec.classBuilder(className)
    .addModifiers(KModifier.DATA)
    .addAnnotation(TypeNames.Serializable)
    .addDescription(description)
    .primaryConstructor(
      FunSpec.constructorBuilder()
        .addParameters(
          properties.map { property ->
            ParameterSpec
              .builder(
                property.name,
                property.type.copy(
                  nullable = property.nullable
                )
              )
              .apply {
                if (property.nullable) {
                  defaultValue(property.defaultValue())
                }
                addDescription(property.description)
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
              nullable = property.nullable
            )
          )
          .initializer(property.name)
          .build()
      }
    )
    .apply {
      val allRequirements = properties.associateWith { it.requirements }.filterValues { it.isNotEmpty() }
      if (allRequirements.isNotEmpty()) {
        addInitializerBlock(
          buildCodeBlock {
              allRequirements.forEach { (property, requirements) ->
                val name = MemberName(className, property.name)
                val nullable = property.nullable

                requirements.forEach { requirement ->
                  val (accessor, operator, value) = when (requirement) {
                    is Requirement.MinValue -> listOf("", ">=", requirement.minValue)
                    is Requirement.MaxValue -> listOf("", "<=", requirement.maxValue)
                    is Requirement.MinLength -> listOf(".count()", ">=", requirement.minLength)
                    is Requirement.MaxLength -> listOf(".count()", "<=", requirement.maxLength)
                  }

                  add("require(")
                  if (nullable) {
                    add("%N == null || ", name)
                  }
                  add("%N$accessor $operator %L", name, value)

                  beginControlFlow(")")
                  val args = arrayOf(name.simpleName, value, name)
                  if (accessor.toString().isEmpty()) {
                    addStatement("\"%L must be $operator %L, but was \$%N\"", *args)
                  } else if (!nullable) {
                    addStatement("\"%L$accessor must be $operator %L, but was \${%N$accessor}\"", *args)
                  } else {
                    addStatement("\"%L$accessor must be $operator %L, but was \${%N?$accessor}\"", *args)
                  }
                  endControlFlow()
                }
              }
            }
        )
      }
    }
    .build()
}

fun createValueClass(
  className: ClassName,
  serialName: String?,
  innerType: TypeName,
  additionalConfiguration: TypeSpec.Builder.() -> Unit = {},
): List<TypeSpec> {
  val valueClassTypeSpec = TypeSpec.classBuilder(className)
    .addModifiers(KModifier.VALUE)
    .addAnnotation(JvmInline::class)
    .apply {
      addAnnotation(TypeNames.Serializable)
      if (serialName != null) {
        addAnnotation(
          AnnotationSpec.builder(TypeNames.SerialName)
            .addMember("%S", serialName)
            .build()
        )
      }
    }
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

  return listOf(valueClassTypeSpec)
}

fun createObjectClass(
  className: ClassName,
  description: String?,
): TypeSpec {
  return TypeSpec.objectBuilder(className)
    .addAnnotation(TypeNames.Serializable)
    .addDescription(description)
    .build()
}

fun createOpenEnumClass(
  enumClass: EnumClass,
  entries: Collection<EnumEntry>,
  generateUnknown: Boolean,
): List<TypeSpec> {
  val className = enumClass.className

  val valueOf = if (generateUnknown) "safeValueOf" else "unsafeValueOf"
  val serializerClassName = className.peerClass(className.simpleName + "Serializer")
  val serializerTypeSpec = TypeSpec.classBuilder(serializerClassName)
    .addSuperinterface(
      TypeNames.KSerializer.parameterizedBy(className),
      CodeBlock.of("%M(%T::$valueOf)".trimIndent(), stringEnumSerializer, className)
    )
    .build()

  val sealedClass = TypeSpec.classBuilder(className)
    .addModifiers(KModifier.SEALED)
    .addAnnotation(
      AnnotationSpec.builder(TypeNames.Serializable)
        .addMember("with = %T::class", serializerClassName)
        .build()
    )
    .addDescription(enumClass.description)
    .superclass(TypeNames.AtpEnum)
    .primaryConstructor(
      FunSpec.constructorBuilder()
        .addParameter("value", STRING)
        .build()
    )
    .addProperty(
      PropertySpec.builder("value", STRING, KModifier.OVERRIDE)
        .initializer("value")
        .build()
    )

  val superToString: FunSpec =
    FunSpec.builder("toString")
      .addModifiers(KModifier.OVERRIDE)
      .returns(STRING)
      .addStatement("return super.toString()")
      .build()

  val valueOfControlFlow = CodeBlock.builder()
    .beginControlFlow("return when (value)")

  entries.forEach { entry ->
    val entryClassName = entry.name.substringAfterLast('#').toPascalCase()

    valueOfControlFlow.addStatement("%S -> %L", entry.name, entryClassName)
    sealedClass.addType(
      TypeSpec.objectBuilder(entryClassName)
        .addModifiers(KModifier.DATA)
        .addDescription(entry.description)
        .superclass(className)
        .addSuperclassConstructorParameter("%S", entry.name)
        .addFunction(superToString)
        .build()
    )
  }

  // Avoid conflicting names by appending _ to the unknown name
  val safeUnknownEntryName = "Unknown".let {
    if (it in sealedClass.typeSpecs.map { type -> type.name }) "_$it" else it
  }

  if (generateUnknown) {
    sealedClass.addType(
      TypeSpec.classBuilder(safeUnknownEntryName)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter(
              ParameterSpec
                .builder("rawValue", STRING)
                .build()
            )
            .build()
        )
        .addProperty(
          PropertySpec.builder("rawValue", STRING)
            .initializer("rawValue")
            .build()
        )
        .superclass(className)
        .addSuperclassConstructorParameter("""rawValue""")
        .build()
    )
  }

  sealedClass.addType(
    TypeSpec.companionObjectBuilder()
      .addFunction(
        FunSpec.builder(valueOf)
          .addParameter(
            ParameterSpec.builder("value", STRING).build()
          )
          .returns(className)
          .addCode(
            valueOfControlFlow
              .apply {
                if (generateUnknown) {
                  addStatement("else -> $safeUnknownEntryName(value)")
                } else {
                  addStatement("else -> error(%P)", "Unknown value: '\$value'")
                }
              }
              .endControlFlow()
              .build()
          )
          .build()
      )
      .build()
  )

  return listOf(serializerTypeSpec, sealedClass.build())
}

fun LexiconSingleReference.typeName(
  environment: LexiconProcessingEnvironment,
  source: LexiconDocument
): TypeName {
  val lexiconRefType = environment.loadReference(source, this)
  if (lexiconRefType is LexiconString && lexiconRefType.isEnumReference()) {
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

  val referenceClassName = ClassName(packageName, className)

  return if (lexiconRefType is LexiconArray) {
    LIST.parameterizedBy(referenceClassName)
  } else {
    referenceClassName
  }
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
    }.let { LIST.parameterizedBy(it) }
  }
  is LexiconBlob -> {
    TypeNames.Blob
  }
  is LexiconIpldType -> {
    environment.defaults.binaryDataType.className()
  }
  is LexiconObject -> {
    val sourceId = context.document.id
    val packageName = sourceId.substringBeforeLast(".")
    val className = sourceId.substringAfterLast(".").capitalized() + propertyName!!.capitalized()
    ClassName(packageName, className)
  }
  is LexiconPrimitive -> {
    context.primitiveTypeName(userType, propertyName!!)
  }
  is LexiconRecord -> {
    typeName(environment, context, userType.key, userType.record)
  }
  is LexiconToken -> {
    STRING
  }
  is LexiconXrpcProcedure,
  is LexiconXrpcQuery,
  is LexiconXrpcSubscription -> {
    val sourceId = context.document.id
    val packageName = sourceId.substringBeforeLast(".")
    val className = sourceId.substringAfterLast(".").capitalized() + propertyName!!.capitalized()
    ClassName(packageName, className)
  }
}

private val CAMEL_CASE_REGEX = "(?<=[a-zA-Z])[A-Z]".toRegex()

fun String.toEnumCase(): String {
  val sanitized = this
    .trimStart { !it.isJavaIdentifierStart() }
    .map { if (!it.isJavaIdentifierPart()) "_" else it }
    .joinToString("")

  return CAMEL_CASE_REGEX.replace(sanitized) { "_${it.value}" }.uppercase().replace('-', '_')
}

fun String.toPascalCase() = toEnumCase()
  .split('_')
  .joinToString("") { s ->
    s.lowercase().replaceFirstChar { it.uppercase() }
  }

internal fun <T> T.addDescription(description: String?): T
    where T : Annotatable.Builder<T>,
          T : Documentable.Builder<T> = apply {
  if (description != null) {
    if (description.isDeprecated()) {
      addAnnotation(
        AnnotationSpec.builder(TypeNames.Deprecated)
          .addMember("%S", description)
          .build()
      )
    } else {
      // Inform kotlinpoet that any space character can be safely wrapped onto a new line
      addKdoc(description.replace(" ", "♢"))
    }
  }
}

internal fun String?.isDeprecated(): Boolean = this != null && startsWith("deprecated", ignoreCase = true)

internal fun String.capitalized(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

internal fun List<String>.commonPrefix(): String = when (size) {
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
}.let { prefix ->
  if (prefix in this) {
    prefix.substringBefore('#') + '#'
  } else {
    prefix
  }
}

internal fun List<String>.commonSuffix(): String = when (size) {
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


private fun TypeName.hasClassName(className: ClassName): Boolean = className in classNames()

private fun TypeName.classNames(): Sequence<ClassName> = sequence {
  when (this@classNames) {
    is ClassName -> yield(this@classNames)
    is Dynamic -> Unit
    is LambdaTypeName -> {
      receiver?.let { yieldAll(it.classNames()) }
      yieldAll(returnType.classNames())
      yieldAll(parameters.flatMap { it.type.classNames() })
    }
    is ParameterizedTypeName -> {
      yieldAll(rawType.classNames())
      yieldAll(typeArguments.flatMap { it.classNames() })
    }
    is TypeVariableName -> {
      yieldAll(bounds.flatMap { it.classNames() })
    }
    is WildcardTypeName -> {
      yieldAll(inTypes.flatMap { it.classNames() })
      yieldAll(outTypes.flatMap { it.classNames() })
    }
  }
}.distinct()
