package sh.christian.ozone.jetstream

/**
 * Known hosted instances of the Jetstream service.
 */
enum class JetstreamHost(
  val region: String,
) {
  US_EAST("us-east"),
  US_WEST("us-west"),
}
