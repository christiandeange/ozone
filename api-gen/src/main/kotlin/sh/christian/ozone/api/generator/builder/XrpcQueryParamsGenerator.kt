package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.withIndent
import sh.christian.ozone.api.lexicon.LexiconAudio
import sh.christian.ozone.api.lexicon.LexiconBlob
import sh.christian.ozone.api.lexicon.LexiconImage
import sh.christian.ozone.api.lexicon.LexiconObject
import sh.christian.ozone.api.lexicon.LexiconPrimitive
import sh.christian.ozone.api.lexicon.LexiconRecord
import sh.christian.ozone.api.lexicon.LexiconToken
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.LexiconVideo
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery

class XrpcQueryParamsGenerator : TypesGenerator {
  override fun generateTypes(
    params: BuilderParams,
    userType: LexiconUserType,
  ): List<TypeSpec> = when (userType) {
    is LexiconXrpcProcedure -> createQueryParamsType(params, userType.parameters)
    is LexiconXrpcQuery -> createQueryParamsType(params, userType.parameters)

    is LexiconAudio,
    is LexiconBlob,
    is LexiconImage,
    is LexiconObject,
    is LexiconRecord,
    is LexiconToken,
    is LexiconVideo -> emptyList()
  }

  private fun createQueryParamsType(
    params: BuilderParams,
    queryParams: Map<String, LexiconPrimitive>,
  ): List<TypeSpec> {
    if (queryParams.isEmpty()) return emptyList()

    val properties: List<SimpleProperty> = queryParams.map { (name, prop) ->
      SimpleProperty(name, prop.type.toTypeName(nullable = false))
    }

    return listOf(
      createDataClass(
        className = "${params.classPrefix}QueryParams",
        properties = properties,
        additionalConfiguration = {
          addFunction(toMap(properties))

          // Allows for custom static extension methods on the generated type.
          addType(TypeSpec.companionObjectBuilder().build())
        }
      )
    )
  }

  private fun toMap(properties: List<SimpleProperty>): FunSpec {
    val stringStringMap = MAP.parameterizedBy(STRING, STRING)

    return FunSpec.builder("toMap")
      .returns(stringStringMap)
      .addCode(
        CodeBlock.builder()
          .add("return mapOf(\n")
          .withIndent {
            properties.forEach { property ->
              val maybeToString = if (property.type == STRING) {
                ""
              } else {
                ".toString()"
              }

              add("\"%L\" to %L%L,\n", property.name, property.name, maybeToString)
            }
          }
          .add(")")
          .build()
      )
      .build()
  }
}
