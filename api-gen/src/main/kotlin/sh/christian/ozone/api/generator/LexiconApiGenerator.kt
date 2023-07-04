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

  fun generateApi() {
    val packageName = "sh.christian.atp.api"
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
      inputContentType = procedure.input?.encoding,
    )
  }

  private fun LexiconXrpcParameters.type(context: GeneratorContext): ApiType? {
    return ApiType(
      className = ClassName(context.authority, "${context.classPrefix}QueryParams"),
      description = description,
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
            returns(TypeNames.AtpResponse.parameterizedBy(outputType.className))
          }

          is Procedure -> {
            if (inputType != null) {
              addParameter(
                ParameterSpec.builder("request", inputType.className)
                  .apply { inputType.description?.let { addKdoc(it) } }
                  .build()
              )
            }
            returns(TypeNames.AtpResponse.parameterizedBy(outputType?.className ?: UNIT))
          }
        }
      }
      .apply(block)
      .build()
  }

  private fun generateAtpApi(packageName: String) {
    val interfaceType = TypeSpec.interfaceBuilder(ClassName(packageName, "AtpApi"))
      .apply {
        apiCalls.forEach { apiCall ->
          addFunction(apiCall.toFunctionSpec {
            addModifiers(KModifier.ABSTRACT)
          })
        }
      }.build()

    FileSpec.builder(packageName, "AtpApi")
      .addType(interfaceType)
      .build()
      .writeTo(environment.outputDirectory)
  }

  private fun generateXrpcApi(packageName: String) {
    val classType = TypeSpec.classBuilder(ClassName(packageName, "XrpcApi"))
      .addSuperinterface(ClassName(packageName, "AtpApi"))
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
        apiCalls.forEach { apiCall ->
          addFunction(apiCall.toFunctionSpec {
            val code = CodeBlock.builder()
              .apply {
                val path = "/xrpc/${apiCall.id}"
                when (apiCall) {
                  is Query -> {
                    val args = arrayOf(query, path, toAtpResponse)
                    if (apiCall.propertiesType == null) {
                      add("return client.%M(%S).%M()", *args)
                    } else {
                      add("return client.%M(%S, params.asList()).%M()", *args)
                    }
                  }
                  is Procedure -> {
                    val args = arrayOf(procedure, path, toAtpResponse)
                    if (apiCall.inputType == null) {
                      add("return client.%M(%S).%M()", *args)
                    } else {
                      add("return client.%M(%S, request).%M()", *args)
                    }
                  }
                }
              }
              .build()

            addModifiers(KModifier.OVERRIDE)
            addCode(code)
          })
        }
      }.build()

    FileSpec.builder(packageName, "XrpcApi")
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
      val inputContentType: String?,
    ) : ApiCall
  }

  private data class ApiType(
    val className: ClassName,
    val description: String?,
  )
}
