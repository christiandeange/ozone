package sh.christian.ozone.model

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class Delta(
  val duration: Duration,
)