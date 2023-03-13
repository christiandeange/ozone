package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import sh.christian.ozone.api.generator.ENCODING
import sh.christian.ozone.api.generator.SERIALIZABLE
import sh.christian.ozone.api.lexicon.LexiconArrayItem
import sh.christian.ozone.api.lexicon.LexiconAudio
import sh.christian.ozone.api.lexicon.LexiconBlob
import sh.christian.ozone.api.lexicon.LexiconImage
import sh.christian.ozone.api.lexicon.LexiconObject
import sh.christian.ozone.api.lexicon.LexiconObjectProperty
import sh.christian.ozone.api.lexicon.LexiconRecord
import sh.christian.ozone.api.lexicon.LexiconToken
import sh.christian.ozone.api.lexicon.LexiconUserType
import sh.christian.ozone.api.lexicon.LexiconVideo
import sh.christian.ozone.api.lexicon.LexiconXrpcBody
import sh.christian.ozone.api.lexicon.LexiconXrpcProcedure
import sh.christian.ozone.api.lexicon.LexiconXrpcQuery

class XrpcBodyGenerator : TypesGenerator {
  override fun generateTypes(
    params: BuilderParams,
    userType: LexiconUserType
  ): List<TypeSpec> = when (userType) {
    is LexiconXrpcProcedure -> {
      buildList {
        userType.input?.let {
          add(createBodyType("${params.classPrefix}Request", it))
        }
        userType.output?.let {
          add(createBodyType("${params.classPrefix}Response", it))
        }
      }
    }
    is LexiconXrpcQuery -> {
      buildList {
        userType.output?.let {
          add(createBodyType("${params.classPrefix}Response", it))
        }
      }
    }

    is LexiconAudio,
    is LexiconBlob,
    is LexiconImage,
    is LexiconObject,
    is LexiconRecord,
    is LexiconToken,
    is LexiconVideo -> emptyList()
  }

  private fun createBodyType(
    className: String,
    body: LexiconXrpcBody,
  ): TypeSpec {
    val encodings: List<String> = body.encoding.values()

    val properties: List<SimpleProperty> = body.schema.properties.map { (name, prop) ->
      val isNullable = name !in body.schema.required

      when (prop) {
        is LexiconObjectProperty.Array -> {
          when (val items = prop.array.items) {
            is LexiconArrayItem.Primitive -> {
              val propertyType = items.primitive.type.toTypeName(nullable = false)
              SimpleProperty(name, LIST.parameterizedBy(propertyType).copy(isNullable))
            }

            is LexiconArrayItem.Reference -> TODO()
            is LexiconArrayItem.ReferenceList -> TODO()
          }
        }

        is LexiconObjectProperty.Primitive -> {
          SimpleProperty(name, prop.primitive.type.toTypeName(isNullable))
        }

        is LexiconObjectProperty.Reference -> TODO()
        is LexiconObjectProperty.ReferenceList -> TODO()
      }
    }

    return createDataClass(
      className = className,
      properties = properties,
      additionalConfiguration = {
        addAnnotation(SERIALIZABLE)
        addAnnotation(
          AnnotationSpec.builder(ENCODING)
            .addMember(encodings.joinToString(", ") { "%S" }, *encodings.toTypedArray())
            .build()
        )

        // Allows for custom static extension methods on the generated type.
        addType(TypeSpec.companionObjectBuilder().build())
      },
    )
  }
}
