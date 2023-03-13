package sh.christian.ozone.api.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import sh.christian.ozone.api.generator.LexiconClassCreator
import sh.christian.ozone.api.lexicon.loadDocument

//@CacheableTask
abstract class LexiconGeneratorTask : DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  abstract val schemasClasspath: ConfigurableFileCollection

  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  @TaskAction
  fun generateSchemaClasses() {
    val outputDir = outputDirectory.asFile.get()
    outputDir.deleteRecursively()
    outputDir.mkdirs()

    val lexiconClassCreator = LexiconClassCreator(outputDir)
    lexiconClassCreator.createCommonClasses()

    println("Input schema files:")
    schemasClasspath.forEach {
      println("  - ${it.absolutePath}")
      processFile(
        lexiconClassCreator = lexiconClassCreator,
        fileName = it.name,
        json = it.readText(),
      )
    }
  }

  private fun processFile(
    lexiconClassCreator: LexiconClassCreator,
    fileName: String,
    json: String,
  ) {
    try {
      val lexiconDocument = loadDocument(json)
      lexiconClassCreator.createClassForLexicon(lexiconDocument)
    } catch (e: Exception) {
      throw IllegalArgumentException("Failed to process $fileName", e)
    }
  }
}
