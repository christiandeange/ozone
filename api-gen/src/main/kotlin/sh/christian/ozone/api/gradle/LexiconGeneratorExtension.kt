package sh.christian.ozone.api.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property
import sh.christian.ozone.api.generator.ApiConfiguration
import sh.christian.ozone.api.generator.ApiReturnType
import sh.christian.ozone.api.generator.ApiReturnType.Raw
import javax.inject.Inject

abstract class LexiconGeneratorExtension
@Inject constructor(
  objects: ObjectFactory,
  projectLayout: ProjectLayout,
) {
  private val apiName: Property<String?> =
    objects.property<String?>().convention(null)

  private val returnType: Property<ApiReturnType> =
    objects.property<ApiReturnType>().convention(Raw)

  internal val apiConfiguration: Provider<ApiConfiguration> =
    apiName
      .zip<ApiReturnType, ApiConfiguration>(returnType) { apiName, returnType ->
        ApiConfiguration.GenerateApiConfiguration(apiName!!, returnType)
      }
      .orElse(ApiConfiguration.None)

  val outputDirectory: DirectoryProperty =
    objects.directoryProperty().convention(
      projectLayout.buildDirectory
        .dir("generated")
        .map { it.dir("lexicons") }
    )

  fun generateApi(
    name: String,
    configure: ApiGeneratorExtension.() -> Unit = {},
  ) {
    apiName.set(name)
    apiName.disallowChanges()

    ApiGeneratorExtension(returnType).apply {
      configure()
      returnType.disallowChanges()
    }
  }

  class ApiGeneratorExtension internal constructor(
    val returnType: Property<ApiReturnType>,
  )
}
