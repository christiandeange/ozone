package sh.christian.ozone.api

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual val engine: HttpClientEngineFactory<*> get() = Darwin

actual object OzoneDispatchers {
  actual val IO: CoroutineDispatcher get() = Dispatchers.IO
}
