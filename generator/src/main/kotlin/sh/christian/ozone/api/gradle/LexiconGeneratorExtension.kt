package sh.christian.ozone.api.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import sh.christian.ozone.api.generator.ApiConfiguration
import sh.christian.ozone.api.generator.ApiReturnType
import sh.christian.ozone.api.generator.ApiReturnType.Raw
import javax.inject.Inject

abstract class LexiconGeneratorExtension
@Inject constructor(
  private val objects: ObjectFactory,
  projectLayout: ProjectLayout,
) {
  internal val apiConfigurations: ListProperty<ApiConfiguration> =
    objects.listProperty<ApiConfiguration>().convention(emptyList())

  val outputDirectory: DirectoryProperty =
    objects.directoryProperty().convention(
      projectLayout.buildDirectory
        .map { it.dir("generated") }
        .map { it.dir("lexicons") }
    )

  fun generateApi(
    name: String,
    configure: ApiGeneratorExtension.() -> Unit = {},
  ) {
    apiConfigurations.add(
      ApiGeneratorExtension(name, objects)
        .apply(configure)
        .apiConfiguration
    )
  }

  class ApiGeneratorExtension internal constructor(
    internal val name: String,
    objects: ObjectFactory,
  ) {
    val packageName: Property<String> =
      objects.property<String>().convention("sh.christian.ozone")

    val suspending: Property<Boolean> =
      objects.property<Boolean>().convention(true)

    private val implementationName: Property<String?> =
      objects.property<String?>().convention(null)

    val returnType: Property<ApiReturnType> =
      objects.property<ApiReturnType>().convention(Raw)

    fun withKtorImplementation(name: String) {
      implementationName.set(name)
    }

    internal val apiConfiguration by lazy {
      ApiConfiguration(
        packageName = packageName.readFinalizedValue(),
        interfaceName = name,
        implementationName = implementationName.readFinalizedValueOrNull(),
        suspending = suspending.readFinalizedValue(),
        returnType = returnType.readFinalizedValue(),
      )
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
