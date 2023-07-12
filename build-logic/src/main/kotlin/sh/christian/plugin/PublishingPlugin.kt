package sh.christian.plugin

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTaskPartial

@Suppress("UnstableApiUsage", "unused")
class PublishingPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val groupId = target.stringProperty("POM_GROUP_ID")
    val artifactId = target.path.trimStart(':').replace(':', '-')
    val version = target.stringProperty("POM_VERSION")

    target.group = groupId
    target.version = version

    target.plugins.apply("com.vanniktech.maven.publish")
    target.plugins.apply("org.jetbrains.dokka")

    target.tasks.withType<DokkaTaskPartial>().configureEach {
      dokkaSourceSets.configureEach {
        suppress.set("internal" in project.name)
        suppressGeneratedFiles.set(false)
        reportUndocumented.set(true)
      }
    }

    target.pluginManager.withPlugin("sh.christian.ozone.api-gen") {
      target.tasks.withType<DokkaTaskPartial>().configureEach {
        dependsOn(target.tasks.named("generateLexicons"))
      }
    }

    target.extensions.configure<MavenPublishBaseExtension> {
      coordinates(
        groupId = groupId,
        artifactId = artifactId,
        version = version,
      )

      pom {
        name.set(target.stringProperty("POM_NAME"))
        description.set(target.stringProperty("POM_DESCRIPTION"))
        inceptionYear.set("2023")
        url.set("https://github.com/christiandeange/ozone")

        licenses {
          license {
            name.set("MIT License")
            url.set("https://opensource.org/license/mit/")
            distribution.set("repo")
          }
        }

        developers {
          developer {
            id.set("christiandeange")
            name.set("Christian De Angelis")
            url.set("https://github.com/christiandeange")
          }
        }

        scm {
          url.set("https://github.com/christiandeange/ozone")
          connection.set("scm:git:git://github.com/christiandeange/ozone.git")
          developerConnection.set("scm:git:ssh://git@github.com/christiandeange/ozone.git")
        }
      }

      publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)

      if (!target.findProperty("signing.password")?.toString().isNullOrEmpty()) {
        signAllPublications()
      }
    }
  }

  private fun Project.stringProperty(name: String): String {
    return property(name) as String
  }
}
