package sh.christian.ozone.api.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.intellij.lang.annotations.Language
import sh.christian.ozone.api.generator.ApiConfiguration
import sh.christian.ozone.api.generator.ApiReturnType
import sh.christian.ozone.api.generator.ApiReturnType.Raw
import sh.christian.ozone.api.generator.DefaultsConfiguration
import javax.inject.Inject

abstract class LexiconGeneratorExtension
@Inject constructor(
  private val objects: ObjectFactory,
  projectLayout: ProjectLayout,
) {
  internal val apiConfigurations: ListProperty<ApiConfiguration> =
    objects.listProperty<ApiConfiguration>().convention(emptyList())

  val namespace: Property<String> =
    objects.property<String>().convention("sh.christian.ozone")

  internal val defaults = GeneratorDefaults(objects)

  val outputDirectory: DirectoryProperty =
    objects.directoryProperty().convention(
      projectLayout.buildDirectory
        .map { it.dir("generated") }
        .map { it.dir("lexicons") }
    )

  fun defaults(configure: GeneratorDefaults.() -> Unit) {
    defaults.configure()
  }

  fun generateApi(
    name: String,
    configure: ApiGeneratorExtension.() -> Unit = {},
  ) {
    apiConfigurations.add(
      ApiGeneratorExtension(name, objects)
        .apply(configure)
        .buildApiConfiguration(namespace.readFinalizedValue())
    )
  }

  class GeneratorDefaults internal constructor(
    objects: ObjectFactory,
  ) {
    val generateUnknownsForSealedTypes: Property<Boolean> =
      objects.property<Boolean>().convention(false)

    internal fun buildDefaultsConfiguration(): DefaultsConfiguration {
      return DefaultsConfiguration(
        generateUnknownsForSealedTypes = generateUnknownsForSealedTypes.readFinalizedValue(),
      )
    }
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

    private val includeMethods: ListProperty<Regex> =
      objects.listProperty<Regex>().convention(listOf(".*".toRegex()))

    private val excludeMethods: ListProperty<Regex> =
      objects.listProperty<Regex>().convention(emptyList())

    fun include(@Language("RegExp") pattern: String) {
      includeMethods.add(pattern.toRegex())
    }

    fun exclude(@Language("RegExp") pattern: String) {
      excludeMethods.add(pattern.toRegex())
    }

    fun withKtorImplementation(name: String) {
      implementationName.set(name)
    }

    internal fun buildApiConfiguration(namespace: String): ApiConfiguration {
      return ApiConfiguration(
        namespace = namespace,
        packageName = packageName.readFinalizedValue(),
        interfaceName = name,
        implementationName = implementationName.readFinalizedValueOrNull(),
        suspending = suspending.readFinalizedValue(),
        returnType = returnType.readFinalizedValue(),
        includeMethods = includeMethods.readFinalizedValue(),
        excludeMethods = excludeMethods.readFinalizedValue(),
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

private fun <T> ListProperty<T>.readFinalizedValue(): List<T> {
  finalizeValue()
  return get()
}
