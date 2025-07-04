package sh.christian.ozone.api

import kotlinx.coroutines.CoroutineDispatcher

expect object OzoneDispatchers {
  val IO: CoroutineDispatcher
}
