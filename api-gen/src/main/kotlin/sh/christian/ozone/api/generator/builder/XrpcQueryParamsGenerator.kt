package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.withIndent
import sh.christian.ozone.api.generator.LexiconProcessingEnvironment
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.LexiconXrpcParameter
import sh.christian.ozone.api.lexicon.LexiconXrpcParameters
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery

class XrpcQueryParamsGenerator(
  private val environment: LexiconProcessingEnvironment,
) : TypesGenerator {
  override fun generateTypes(
    context: GeneratorContext,
    userType: LexiconUserType,
  ) {
    when (userType) {
      is LexiconXrpcProcedure -> createQueryParamsType(context, userType.parameters)
      is LexiconXrpcQuery -> createQueryParamsType(context, userType.parameters)
      else -> Unit
    }
  }

  private fun createQueryParamsType(
    context: GeneratorContext,
    queryParams: LexiconXrpcParameters?,
  ) {
    if (queryParams == null || queryParams.properties.isEmpty()) return

    val properties: List<SimpleProperty> = queryParams.properties.map { (name, prop) ->
      when (prop) {
        is LexiconXrpcParameter.Primitive -> {
          SimpleProperty(
            name = name,
            type = prop.primitive.toTypeName(nullable = false),
          )
        }
        is LexiconXrpcParameter.PrimitiveArray -> {
          SimpleProperty(
            name = name,
            type = LIST.parameterizedBy(prop.array.items.toTypeName(nullable = false)),
          )
        }
      }
    }

    context.addType(
      createDataClass(
        className = "${context.classPrefix}QueryParams",
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
