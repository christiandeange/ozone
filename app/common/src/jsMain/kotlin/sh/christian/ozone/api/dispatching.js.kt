package sh.christian.ozone.api

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val engine: HttpClientEngineFactory<*> get() = Js

actual object OzoneDispatchers {
  actual val IO: CoroutineDispatcher get() = Dispatchers.Default
}
