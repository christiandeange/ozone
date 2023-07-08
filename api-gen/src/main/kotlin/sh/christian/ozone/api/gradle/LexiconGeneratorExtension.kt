package sh.christian.ozone.api.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class LexiconGeneratorExtension
@Inject constructor(
  objects: ObjectFactory,
  projectLayout: ProjectLayout,
) {
  val apiName: Property<String> =
    objects.property<String>().convention("AtpApi")

  val outputDirectory: DirectoryProperty =
    objects.directoryProperty().convention(
      projectLayout.buildDirectory
        .dir("generated")
        .map { it.dir("lexicons") }
    )
}
