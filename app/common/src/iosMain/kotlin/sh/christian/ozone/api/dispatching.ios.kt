package sh.christian.ozone.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual object OzoneDispatchers {
  actual val IO: CoroutineDispatcher get() = Dispatchers.IO
}
