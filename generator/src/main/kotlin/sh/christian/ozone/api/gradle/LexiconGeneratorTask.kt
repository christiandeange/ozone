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
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

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
      allLexiconSchemaJsons = schemasClasspath.flatMap { inputFile ->
        inputFile.toPath().findJsonFiles()
      },
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

    lexiconClassFileCreator.generateSealedRelationshipMapping()
    lexiconApiGenerator.generateApis()
  }

  private fun Path.findJsonFiles(): List<String> {
    return when (extension) {
      "json" -> {
        logger.info("Including lexicon schema: $this")
        listOf(readText())
      }

      "jar" -> {
        // Unzip jar and resolve all json files
        logger.info("Including lexicon schemas from: $this")
        FileSystems.newFileSystem(this, null as ClassLoader?).use { jar ->
          jar.rootDirectories.flatMap { root ->
            Files.walk(root)
              .filter { it.isRegularFile() }
              .collect(Collectors.toList())
              .flatMap { it.findJsonFiles() }
          }
        }
      }

      else -> {
        logger.info("Ignoring non-json file: $this")
        emptyList()
      }
    }
  }
}
