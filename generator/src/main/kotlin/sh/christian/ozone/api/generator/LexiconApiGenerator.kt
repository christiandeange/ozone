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
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import sh.christian.ozone.api.generator.LexiconApiGenerator.ApiCall.Procedure
import sh.christian.ozone.api.generator.LexiconApiGenerator.ApiCall.Query
import sh.christian.ozone.api.generator.TypeNames.AtpResponse
import sh.christian.ozone.api.generator.TypeNames.Result
import sh.christian.ozone.api.generator.builder.GeneratorContext
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
      is LexiconToken,
      is LexiconXrpcSubscription -> return

      is LexiconXrpcQuery -> processQuery(context, mainDefinition)
      is LexiconXrpcProcedure -> processProcedure(context, mainDefinition)
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

  private fun LexiconXrpcParameters.type(context: GeneratorContext): ApiType? {
    return ApiType(
      className = ClassName(context.authority, "${context.classPrefix}QueryParams"),
      description = description,
      encoding = "",
    ).takeIf { properties.isNotEmpty() }
  }

  private fun LexiconXrpcBody.type(context: GeneratorContext, suffix: String): ApiType {
    return ApiType(
      className = when (encoding) {
        "*/*" -> BYTE_ARRAY
        "application/vnd.ipld.car" -> BYTE_ARRAY
        else -> ClassName(context.authority, "${context.classPrefix}$suffix")
      },
      description = description,
      encoding = encoding,
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
        }
        val outputClassName = when (this@toFunctionSpec) {
          is Query -> outputType.className
          is Procedure -> outputType?.className ?: UNIT
        }
        val methodReturnClassName = when (returnType) {
          ApiReturnType.Raw -> outputClassName
          ApiReturnType.Result -> Result.parameterizedBy(outputClassName)
          ApiReturnType.Response -> AtpResponse.parameterizedBy(outputClassName)
        }

        description?.let { description ->
          addKdoc(description)
        }

        if (inputType != null) {
          addParameter(
            ParameterSpec.builder(name, inputType.className)
              .apply { inputType.description?.let { addKdoc(it) } }
              .build()
          )
        }

        returns(methodReturnClassName)
      }
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
      .addProperty(
        PropertySpec
          .builder("client", TypeNames.HttpClient)
          .addModifiers(KModifier.PRIVATE)
          .initializer(CodeBlock.of("httpClient.%M()", withJsonConfiguration))
          .build()
      )
      .apply {
        apiCalls.sortedBy { it.name }.forEach { apiCall ->
          addFunction(apiCall.toFunctionSpec(configuration.returnType) {
            if (configuration.suspending) {
              addModifiers(KModifier.SUSPEND)
            }

            val code = CodeBlock.builder()
              .apply {
                val methodName = when (apiCall) {
                  is Query -> query
                  is Procedure -> procedure
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
                if (apiCall is Query && apiCall.propertiesType != null) {
                  add("queryParams = params.asList(),\n")
                }
                if (apiCall is Procedure && apiCall.inputType != null) {
                  add("body = request,\n")
                  add("encoding = %S,\n", apiCall.inputType.encoding)
                }
                unindent()
                add(").%M()", transformingMethodName)

                if (!configuration.suspending) {
                  add("\n")
                  endControlFlow()
                }
              }
              .build()

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
  }

  private data class ApiType(
    val className: ClassName,
    val description: String?,
    val encoding: String,
  )
}
