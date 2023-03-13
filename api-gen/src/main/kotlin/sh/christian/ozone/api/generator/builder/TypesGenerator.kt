package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import sh.christian.ozone.api.lexicon.LexiconPrimitive
import sh.christian.ozone.api.lexicon.LexiconUserType

interface TypesGenerator {
  fun generateTypes(
    params: BuilderParams,
    userType: LexiconUserType,
  ): List<TypeSpec>

  fun LexiconPrimitive.Type.toTypeName(nullable: Boolean) = when (this) {
    LexiconPrimitive.Type.BOOLEAN -> BOOLEAN.copy(nullable = nullable)
    LexiconPrimitive.Type.NUMBER -> LONG.copy(nullable = nullable)
    LexiconPrimitive.Type.INTEGER -> INT.copy(nullable = nullable)
    LexiconPrimitive.Type.STRING -> STRING.copy(nullable = nullable)
  }

  fun createDataClass(
    className: String,
    properties: List<SimpleProperty>,
    additionalConfiguration: TypeSpec.Builder.() -> Unit = {},
  ): TypeSpec {
    return TypeSpec.classBuilder(className)
      .addModifiers(KModifier.DATA)
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameters(
            properties.map { property ->
              ParameterSpec
                .builder(property.name, property.type)
                .build()
            }
          )
          .build()
      )
      .addProperties(
        properties.map { property ->
          PropertySpec
            .builder(property.name, property.type)
            .initializer(property.name)
            .build()
        }
      )
      .apply(additionalConfiguration)
      .build()
  }
}
