package sh.christian.ozone.api.generator

import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.buildCodeBlock
import sh.christian.ozone.api.generator.LexiconApiGenerator.ApiCall.Procedure
import sh.christian.ozone.api.generator.LexiconApiGenerator.ApiCall.Query
import sh.christian.ozone.api.generator.LexiconApiGenerator.ApiCall.Subscription
import sh.christian.ozone.api.generator.TypeNames.AtpResponse
import sh.christian.ozone.api.generator.TypeNames.Flow
import sh.christian.ozone.api.generator.TypeNames.Result
import sh.christian.ozone.api.generator.builder.GeneratorContext
import sh.christian.ozone.api.generator.builder.addDescription
import sh.christian.ozone.api.lexicon.LexiconArray
import sh.christian.ozone.api.lexicon.LexiconBlob
import sh.christian.ozone.api.lexicon.LexiconDocument
import sh.christian.ozone.api.lexicon.LexiconIpldType
import sh.christian.ozone.api.lexicon.LexiconObject
import sh.christian.ozone.api.lexicon.LexiconPrimitive
import sh.christian.ozone.api.lexicon.LexiconRecord
import sh.christian.ozone.api.lexicon.LexiconToken
import sh.christian.ozone.api.lexicon.LexiconXrpcBody
import sh.christian.ozone.api.lexicon.LexiconXrpcParameters
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery
import sh.christian.ozone.api.lexicon.LexiconXrpcSubscription
import sh.christian.ozone.api.lexicon.LexiconXrpcSubscriptionMessage

class LexiconApiGenerator(
  private val environment: LexiconProcessingEnvironment,
  private val configurations: List<ApiConfiguration>,
) {
  private val apiCalls = mutableSetOf<ApiCall>()

  fun processDocument(lexiconDocument: LexiconDocument) {
    val mainDefinition = lexiconDocument.defs["main"] ?: return
    val context = GeneratorContext(lexiconDocument, "")

    return when (mainDefinition) {
      is LexiconArray,
      is LexiconBlob,
      is LexiconIpldType,
      is LexiconObject,
      is LexiconPrimitive,
      is LexiconRecord,
      is LexiconToken -> return
      is LexiconXrpcQuery -> processQuery(context, mainDefinition)
      is LexiconXrpcProcedure -> processProcedure(context, mainDefinition)
      is LexiconXrpcSubscription -> processSubscription(context, mainDefinition)
    }
  }

  fun generateApis() {
    configurations.forEach { configuration ->
      generateAtpApi(
        configuration = configuration,
        interfaceName = ClassName(configuration.packageName, configuration.interfaceName),
      )

      configuration.implementationName?.let { implementationName ->
        generateXrpcApi(
          configuration = configuration,
          className = ClassName(configuration.packageName, implementationName),
          interfaceName = ClassName(configuration.packageName, configuration.interfaceName),
        )
      }
    }
  }

  private fun processQuery(context: GeneratorContext, query: LexiconXrpcQuery) {
    apiCalls += Query(
      id = context.document.id,
      name = context.procedureName,
      description = query.description,
      propertiesType = query.parameters?.type(context),
      outputType = query.output!!.type(context, suffix = "Response"),
    )
  }

  private fun processProcedure(context: GeneratorContext, procedure: LexiconXrpcProcedure) {
    apiCalls += Procedure(
      id = context.document.id,
      name = context.procedureName,
      description = procedure.description,
      inputType = procedure.input?.type(context, suffix = "Request"),
      outputType = procedure.output?.type(context, suffix = "Response"),
    )
  }

  private fun processSubscription(context: GeneratorContext, subscription: LexiconXrpcSubscription) {
    apiCalls += Subscription(
      id = context.document.id,
      name = context.procedureName,
      description = subscription.description,
      propertiesType = subscription.parameters?.type(context),
      outputType = subscription.message!!.type(context, suffix = "Message"),
    )
  }

  private fun LexiconXrpcParameters.type(context: GeneratorContext): ApiType? {
    return ApiType(
      typeName = ClassName(context.authority, "${context.classPrefix}QueryParams"),
      description = description,
      encoding = "",
    ).takeIf { properties.isNotEmpty() }
  }

  private fun LexiconXrpcBody.type(context: GeneratorContext, suffix: String): ApiType {
    return ApiType(
      typeName = when (encoding) {
        "*/*" -> BYTE_ARRAY
        "application/vnd.ipld.car" -> BYTE_ARRAY
        "text/plain" -> STRING
        "application/json" -> ClassName(context.authority, "${context.classPrefix}$suffix")
        else -> error("Unknown encoding: $encoding")
      },
      description = description,
      encoding = encoding,
    )
  }

  private fun LexiconXrpcSubscriptionMessage.type(context: GeneratorContext, suffix: String): ApiType {
    return ApiType(
      typeName = ClassName(context.authority, "${context.classPrefix}$suffix"),
      description = description,
      encoding = "",
    )
  }

  private fun ApiCall.toFunctionSpec(
    returnType: ApiReturnType,
    block: FunSpec.Builder.() -> Unit = {},
  ): FunSpec {
    return FunSpec.builder(name)
      .apply {
        val (name, inputType) = when (this@toFunctionSpec) {
          is Query -> "params" to propertiesType
          is Procedure -> "request" to inputType
          is Subscription -> "params" to propertiesType
        }
        val outputType = when (this@toFunctionSpec) {
          is Query -> returnType.wrap(outputType.typeName)
          is Procedure -> returnType.wrap(outputType?.typeName ?: UNIT)
          is Subscription -> Flow.parameterizedBy(returnType.wrap(outputType.typeName))
        }

        if (inputType != null) {
          addParameter(
            ParameterSpec.builder(name, inputType.typeName)
              .addDescription(inputType.description)
              .build()
          )
        }

        returns(outputType)
      }
      .addDescription(description)
      .apply(block)
      .build()
  }

  private fun generateAtpApi(
    configuration: ApiConfiguration,
    interfaceName: ClassName,
  ) {
    val interfaceType = TypeSpec.interfaceBuilder(interfaceName)
      .apply {
        apiCalls.sortedBy { it.name }.forEach { apiCall ->
          addFunction(apiCall.toFunctionSpec(configuration.returnType) {
            if (configuration.suspending) {
              addModifiers(KModifier.SUSPEND)
            }
            addModifiers(KModifier.ABSTRACT)
          })
        }
      }.build()

    FileSpec.builder(interfaceName)
      .addType(interfaceType)
      .build()
      .writeTo(environment.outputDirectory)
  }

  private fun generateXrpcApi(
    configuration: ApiConfiguration,
    className: ClassName,
    interfaceName: ClassName,
  ) {
    val classType = TypeSpec.classBuilder(className)
      .addSuperinterface(interfaceName)
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameter(
            ParameterSpec
              .builder("httpClient", TypeNames.HttpClient)
              .build()
          )
          .build()
      )
      .addFunction(
        FunSpec.constructorBuilder()
          .callThisConstructor(CodeBlock.of("%M", defaultHttpClient))
          .build()
      )
      .addProperty(
        PropertySpec
          .builder("client", TypeNames.HttpClient)
          .addModifiers(KModifier.PRIVATE)
          .initializer(CodeBlock.of("httpClient.%M()", withXrpcConfiguration))
          .build()
      )
      .apply {
        apiCalls.sortedBy { it.name }.forEach { apiCall ->
          addFunction(apiCall.toFunctionSpec(configuration.returnType) {
            if (configuration.suspending) {
              addModifiers(KModifier.SUSPEND)
            }

            val code = buildCodeBlock {
                val methodName = when (apiCall) {
                  is Query -> query
                  is Procedure -> procedure
                  is Subscription -> subscription
                }
                val path = "/xrpc/${apiCall.id}"
                val transformingMethodName = when (configuration.returnType) {
                  ApiReturnType.Raw -> toAtpModel
                  ApiReturnType.Result -> toAtpResult
                  ApiReturnType.Response -> toAtpResponse
                }

                // Workaround to prevent expression methods from being generated.
                add("%L", "")

                if (configuration.suspending) {
                  add("return client.%M(\n", methodName)
                } else {
                  beginControlFlow("return %M {", runBlocking)
                  add("client.%M(\n", methodName)
                }

                indent()
                add("path = %S,\n", path)

                when (apiCall) {
                  is Query -> {
                    if (apiCall.propertiesType != null) {
                      add("queryParams = params.asList(),\n")
                    }
                  }
                  is Procedure -> {
                    if (apiCall.inputType != null) {
                      add("body = request,\n")
                      add("encoding = %S,\n", apiCall.inputType.encoding)
                    }
                  }
                  is Subscription -> {
                    if (apiCall.propertiesType != null) {
                      add("queryParams = params.asList(),\n")
                    }
                  }
                }

                unindent()
                when (apiCall) {
                  is Query -> add(").%M()", transformingMethodName)
                  is Procedure -> add(").%M()", transformingMethodName)
                  is Subscription -> add(").%M(::%M)", transformingMethodName, findSubscriptionSerializer)
                }

                if (!configuration.suspending) {
                  add("\n")
                  endControlFlow()
                }
              }

            addModifiers(KModifier.OVERRIDE)
            addCode(code)
          })
        }
      }.build()

    FileSpec.builder(className)
      .addType(classType)
      .build()
      .writeTo(environment.outputDirectory)
  }

  private fun ApiReturnType.wrap(typeName: TypeName): TypeName = when (this) {
    ApiReturnType.Raw -> typeName
    ApiReturnType.Result -> Result.parameterizedBy(typeName)
    ApiReturnType.Response -> AtpResponse.parameterizedBy(typeName)
  }

  private sealed interface ApiCall {
    val id: String
    val name: String
    val description: String?

    data class Query(
      override val id: String,
      override val name: String,
      override val description: String?,
      val propertiesType: ApiType?,
      val outputType: ApiType,
    ) : ApiCall

    data class Procedure(
      override val id: String,
      override val name: String,
      override val description: String?,
      val inputType: ApiType?,
      val outputType: ApiType?,
    ) : ApiCall

    data class Subscription(
      override val id: String,
      override val name: String,
      override val description: String?,
      val propertiesType: ApiType?,
      val outputType: ApiType,
    ) : ApiCall
  }

  private data class ApiType(
    val typeName: TypeName,
    val description: String?,
    val encoding: String,
  )
}
