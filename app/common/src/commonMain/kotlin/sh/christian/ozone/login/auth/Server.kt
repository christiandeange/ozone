package sh.christian.ozone.login.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Server {
  val host: String

  @Serializable
  @SerialName("bluesky-social")
  object BlueskySocial : Server {
    override val host: String = "https://bsky.social"
  }

  @Serializable
  @SerialName("custom-server")
  data class CustomServer(
    override val host: String,
  ) : Server
}
