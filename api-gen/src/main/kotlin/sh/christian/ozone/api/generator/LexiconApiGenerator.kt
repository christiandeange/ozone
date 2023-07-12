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
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import sh.christian.ozone.api.generator.ApiConfiguration.GenerateApiConfiguration
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
  private val configuration: GenerateApiConfiguration,
) {
  private val apiCalls = mutableSetOf<ApiCall>()
  private val apiName = configuration.interfaceName
  private val xrpcApiName = "Xrpc${apiName}"

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

  fun generateApi() {
    val packageName = "sh.christian.ozone"
    generateAtpApi(packageName)
    generateXrpcApi(packageName)
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
    block: FunSpec.Builder.() -> Unit = {},
  ): FunSpec {
    return FunSpec.builder(name)
      .addModifiers(KModifier.SUSPEND)
      .apply {
        description?.let { description ->
          addKdoc(description)
        }

        when (this@toFunctionSpec) {
          is Query -> {
            if (propertiesType != null) {
              addParameter(
                ParameterSpec.builder("params", propertiesType.className)
                  .apply { propertiesType.description?.let { addKdoc(it) } }
                  .build()
              )
            }
            returns(outputTypeFor(outputType.className))
          }

          is Procedure -> {
            if (inputType != null) {
              addParameter(
                ParameterSpec.builder("request", inputType.className)
                  .apply { inputType.description?.let { addKdoc(it) } }
                  .build()
              )
            }
            returns(outputTypeFor(outputType?.className ?: UNIT))
          }
        }
      }
      .apply(block)
      .build()
  }

  private fun generateAtpApi(packageName: String) {
    val interfaceType = TypeSpec.interfaceBuilder(ClassName(packageName, apiName))
      .apply {
        apiCalls.sortedBy { it.name }.forEach { apiCall ->
          addFunction(apiCall.toFunctionSpec {
            addModifiers(KModifier.ABSTRACT)
          })
        }
      }.build()

    FileSpec.builder(packageName, apiName)
      .addType(interfaceType)
      .build()
      .writeTo(environment.outputDirectory)
  }

  private fun generateXrpcApi(packageName: String) {
    val classType = TypeSpec.classBuilder(ClassName(packageName, xrpcApiName))
      .addSuperinterface(ClassName(packageName, apiName))
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
          addFunction(apiCall.toFunctionSpec {
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

                add("return client.%M(\n", methodName)
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
              }
              .build()

            addModifiers(KModifier.OVERRIDE)
            addCode(code)
          })
        }
      }.build()

    FileSpec.builder(packageName, xrpcApiName)
      .addType(classType)
      .build()
      .writeTo(environment.outputDirectory)
  }

  private fun outputTypeFor(className: ClassName): TypeName = when (configuration.returnType) {
    ApiReturnType.Raw -> className
    ApiReturnType.Result -> Result.parameterizedBy(className)
    ApiReturnType.Response -> AtpResponse.parameterizedBy(className)
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
