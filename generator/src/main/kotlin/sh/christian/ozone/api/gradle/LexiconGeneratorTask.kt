package sh.christian.ozone.api.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import sh.christian.ozone.api.generator.ApiConfiguration
import sh.christian.ozone.api.generator.LexiconApiGenerator
import sh.christian.ozone.api.generator.LexiconClassFileCreator
import sh.christian.ozone.api.generator.LexiconProcessingEnvironment

@CacheableTask
abstract class LexiconGeneratorTask : DefaultTask() {
  @get:InputFiles
  @get:SkipWhenEmpty
  @get:PathSensitive(RELATIVE)
  abstract val schemasClasspath: ConfigurableFileCollection

  @get:Input
  abstract val apiConfigurations: ListProperty<ApiConfiguration>

  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  @TaskAction
  fun generateSchemaClasses() {
    val configurations = apiConfigurations.get()
    val outputDir = outputDirectory.asFile.get()
    outputDir.deleteRecursively()
    outputDir.mkdirs()

    val processingEnvironment = LexiconProcessingEnvironment(
      allLexiconSchemaJsons = schemasClasspath.map { it.readText() },
      outputDirectory = outputDir,
    )

    val lexiconClassFileCreator = LexiconClassFileCreator(environment = processingEnvironment)
    val lexiconApiGenerator = LexiconApiGenerator(processingEnvironment, configurations)

    processingEnvironment.forEach { schemaId ->
      try {
        val lexiconDocument = processingEnvironment.loadDocument(schemaId)
        lexiconClassFileCreator.createClassForLexicon(lexiconDocument)
        lexiconApiGenerator.processDocument(lexiconDocument)
      } catch (e: Exception) {
        throw IllegalArgumentException("Failed to process $schemaId", e)
      }
    }

    lexiconApiGenerator.generateApis()
  }
}
