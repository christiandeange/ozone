package sh.christian.ozone.api.xrpc

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun HttpClient.withXrpcConfiguration(): HttpClient = config {
  val jsonEnvironment = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "${'$'}type"
  }

  install(ContentNegotiation) {
    json(jsonEnvironment)
  }

  install(WebSockets)
}
