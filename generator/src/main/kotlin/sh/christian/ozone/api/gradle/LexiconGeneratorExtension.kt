package sh.christian.ozone.api.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
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
  private val packageName: Property<String> =
    objects.property<String>().convention("sh.christian.ozone")

  private val apiName: Property<String?> =
    objects.property<String?>().convention(null)

  private val suspending: Property<Boolean> =
    objects.property<Boolean>().convention(true)

  private val implementationName: Property<String?> =
    objects.property<String?>().convention(null)

  private val returnType: Property<ApiReturnType> =
    objects.property<ApiReturnType>().convention(Raw)

  internal val apiConfiguration: ApiConfiguration by lazy {
    val interfaceName = apiName.readFinalizedValueOrNull()

    if (interfaceName != null) {
      ApiConfiguration.GenerateApiConfiguration(
        packageName = packageName.readFinalizedValue(),
        interfaceName = interfaceName,
        implementationName = implementationName.readFinalizedValueOrNull(),
        suspending = suspending.readFinalizedValue(),
        returnType = returnType.readFinalizedValue(),
      )
    } else {
      ApiConfiguration.None
    }
  }

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

    ApiGeneratorExtension(
      packageName = packageName,
      implementationName = implementationName,
      suspending = suspending,
      returnType = returnType
    ).apply(configure)
  }

  class ApiGeneratorExtension internal constructor(
    val packageName: Property<String>,
    private val implementationName: Property<String?>,
    val suspending: Property<Boolean>,
    val returnType: Property<ApiReturnType>,
  ) {
    fun withKtorImplementation(name: String) {
      implementationName.set(name)
    }
  }
}

private fun <T> Property<T>.readFinalizedValue(): T {
  finalizeValue()
  return get()
}

private fun <T> Property<T>.readFinalizedValueOrNull(): T? {
  finalizeValue()
  return orNull
}
