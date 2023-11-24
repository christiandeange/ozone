package sh.christian.ozone.api

import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.coroutines.CoroutineDispatcher

expect val engine: HttpClientEngineFactory<*>

expect object OzoneDispatchers {
  val IO: CoroutineDispatcher
}
