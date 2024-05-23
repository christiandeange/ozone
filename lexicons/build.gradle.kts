plugins {
  id("ozone-publish")
  `java-library`
}

tasks.jar.configure {
  from(fileTree("schemas") { include("**/*.json") })
}
