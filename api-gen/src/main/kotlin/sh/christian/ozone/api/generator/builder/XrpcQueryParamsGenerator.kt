package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.COLLECTION
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ITERABLE
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MUTABLE_COLLECTION
import com.squareup.kotlinpoet.MUTABLE_ITERABLE
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.MUTABLE_SET
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
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
      val nullable = name !in queryParams.required

      when (prop) {
        is LexiconXrpcParameter.Primitive -> {
          SimpleProperty(
            name = name,
            type = prop.primitive.toTypeName(),
            nullable = nullable,
            description = prop.primitive.description,
          )
        }
        is LexiconXrpcParameter.PrimitiveArray -> {
          SimpleProperty(
            name = name,
            type = TypeNames.ImmutableList.parameterizedBy(prop.array.items.toTypeName()),
            nullable = nullable,
            description = prop.array.description,
          )
        }
      }
    }

    context.addType(
      createDataClass(
        className = "${context.classPrefix}QueryParams",
        properties = properties,
        description = queryParams.description,
        additionalConfiguration = {
          addFunction(toMap(properties))

          // Allows for custom static extension methods on the generated type.
          addType(TypeSpec.companionObjectBuilder().build())
        }
      )
    )
  }

  private fun toMap(properties: List<SimpleProperty>): FunSpec {
    val returns = TypeNames.ImmutableList.parameterizedBy(
      Pair::class.asClassName().parameterizedBy(STRING, ANY.copy(nullable = true))
    )

    return FunSpec.builder("asList")
      .returns(returns)
      .addCode(
        CodeBlock.builder()
          .add("return buildList {\n")
          .withIndent {
            properties.forEach { property ->
              if (property.type is ParameterizedTypeName && property.type.rawType.isCollection) {
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

  private val ClassName.isCollection
    get() = when (this) {
      ITERABLE,
      COLLECTION,
      LIST,
      SET,
      MAP,
      MUTABLE_ITERABLE,
      MUTABLE_COLLECTION,
      MUTABLE_LIST,
      MUTABLE_SET,
      MUTABLE_MAP,
      TypeNames.ImmutableList -> true
      else -> false
    }
}
