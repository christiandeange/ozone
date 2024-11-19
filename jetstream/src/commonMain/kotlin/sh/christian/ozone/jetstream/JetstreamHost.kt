package sh.christian.ozone.jetstream

/**
 * Known hosted instances of the Jetstream service.
 */
enum class JetstreamHost(
  val instance: Int,
  val region: String,
) {
  JETSTREAM_1_US_EAST(1, "us-east"),
  JETSTREAM_2_US_EAST(2, "us-east"),
  JETSTREAM_1_US_WEST(1, "us-west"),
  JETSTREAM_2_US_WEST(2, "us-west"),
}
