package sh.christian.ozone.api

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val engine: HttpClientEngineFactory<*> get() = CIO

actual object OzoneDispatchers {
  actual val IO: CoroutineDispatcher get() = Dispatchers.IO
}
