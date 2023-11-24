package sh.christian.ozone.api

import com.squareup.workflow1.Worker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import sh.christian.ozone.api.response.AtpResponse

abstract class NetworkWorker<T> : Worker<AtpResponse<T>> {
  override fun run(): Flow<AtpResponse<T>> = flow {
    emit(execute())
  }.flowOn(OzoneDispatchers.IO)

  abstract suspend fun execute(): AtpResponse<T>

  companion object {
    operator fun <T> invoke(
      block: suspend () -> AtpResponse<T>,
    ): NetworkWorker<T> = object : NetworkWorker<T>() {
      override suspend fun execute(): AtpResponse<T> = block()
    }
  }
}
