package sh.christian.ozone.api.runtime

@Retention(AnnotationRetention.RUNTIME)
annotation class Encoding(
  vararg val type: String,
)
