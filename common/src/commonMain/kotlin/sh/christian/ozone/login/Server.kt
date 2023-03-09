package sh.christian.ozone.login

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Server {
  val hostName: String

  @Serializable
  @SerialName("bluesky-social")
  object BlueskySocial : Server {
    override val hostName: String = "https://bsky.social"
  }

  @Serializable
  @SerialName("custom-server")
  data class CustomServer(
    override val hostName: String,
  ) : Server
}
