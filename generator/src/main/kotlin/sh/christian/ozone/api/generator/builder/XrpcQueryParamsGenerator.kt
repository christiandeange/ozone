package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.withIndent
import sh.christian.ozone.api.generator.LexiconProcessingEnvironment
import sh.christian.ozone.api.generator.TypeNames
import sh.christian.ozone.api.generator.toImmutableList
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.LexiconXrpcParameter
import sh.christian.ozone.api.lexicon.LexiconXrpcParameters
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery
import sh.christian.ozone.api.lexicon.LexiconXrpcSubscription

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
      is LexiconXrpcSubscription -> createQueryParamsType(context, userType.parameters)
      else -> Unit
    }
  }

  private fun createQueryParamsType(
    context: GeneratorContext,
    queryParams: LexiconXrpcParameters?,
  ) {
    if (queryParams == null || queryParams.properties.isEmpty()) return

    val properties: List<SimpleProperty> = queryParams.properties.map { (name, prop) ->
      val nullable = name !in queryParams.required

      when (prop) {
        is LexiconXrpcParameter.Primitive -> {
          SimpleProperty(
            name = name,
            type = context.primitiveTypeName(prop.primitive, name),
            nullable = nullable,
            description = prop.primitive.description,
            definedDefault = context.primitiveDefaultValue(prop.primitive, name),
            requirements = prop.primitive.requirements(),
          )
        }
        is LexiconXrpcParameter.PrimitiveArray -> {
          SimpleProperty(
            name = name,
            type = TypeNames.ReadOnlyList.parameterizedBy(context.primitiveTypeName(prop.array.items, name)),
            nullable = nullable,
            description = prop.array.description,
            definedDefault = context.primitiveDefaultValue(prop.array.items, name),
            requirements = prop.array.requirements(),
          )
        }
      }
    }

    context.addType(
      createClassForProperties(
        className = ClassName(context.authority, "${context.classPrefix}QueryParams"),
        properties = properties,
        description = queryParams.description,
      ).toBuilder()
        .addFunction(toMap(properties))
        .build()
    )
  }

  private fun toMap(properties: List<SimpleProperty>): FunSpec {
    val returns = TypeNames.ReadOnlyList.parameterizedBy(
      Pair::class.asClassName().parameterizedBy(STRING, ANY.copy(nullable = true))
    )

    return FunSpec.builder("asList")
      .returns(returns)
      .addCode(
        CodeBlock.builder()
          .add("return buildList {\n")
          .withIndent {
            properties.forEach { property ->
              if (property.isCollection()) {
                add("%L.forEach {\n", property.name)
                withIndent {
                  addStatement("add(%S to it)", property.name)
                }
                add("}\n")
              } else {
                addStatement("add(%S to %L)", property.name, property.name)
              }
            }
          }
          .add("}.%M()", toImmutableList)
          .build()
      )
      .build()
  }
}
