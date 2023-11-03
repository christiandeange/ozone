package sh.christian.ozone.util

import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.toErrorProps

sealed interface RemoteData<T : Any> {
  data class Fetching<T : Any>(
    val previous: T? = null,
  ) : RemoteData<T>

  data class Success<T : Any>(
    val value: T,
  ) : RemoteData<T>

  data class Failed<T : Any>(
    val error: ErrorProps,
    val previous: T? = null,
  ) : RemoteData<T>

  fun getOrNull(): T? = when (this) {
    is Fetching -> previous
    is Success -> value
    is Failed -> previous
  }

  companion object {
    operator fun <T : Any> invoke(value: T) = Success(value)

    fun <T : Any> fromAtpResponseOrError(
      response: AtpResponse<T>,
      previous: RemoteData<T>? = null,
      customErrorProvider: () -> ErrorProps,
    ): RemoteData<T> = when (response) {
      is AtpResponse.Success -> Success(response.response)
      is AtpResponse.Failure -> {
        response.maybeResponse()?.let { Success(it) }
          ?: Failed(
            error = response.toErrorProps(true) ?: customErrorProvider(),
            previous = previous?.getOrNull(),
          )
      }
    }
  }
}
