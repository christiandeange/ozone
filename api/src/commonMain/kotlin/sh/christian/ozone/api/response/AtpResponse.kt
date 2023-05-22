package sh.christian.ozone.api.response

sealed interface AtpResponse<T> {
  data class Success<T>(
    val response: T,
    val headers: Map<String, String>,
  ) : AtpResponse<T>

  data class Failure<T>(
    val statusCode: StatusCode,
    val response: T?,
    val error: AtpErrorDescription?,
    val headers: Map<String, String>,
  ) : AtpResponse<T>

  fun maybeResponse(): T? = when (this) {
    is Success -> response
    is Failure -> response
  }

  fun requireResponse(): T = when (this) {
    is Success -> response
    is Failure -> requireNotNull(response) {
      if (error != null) {
        "Failing request provided error instead of valid response body: $error"
      } else {
        "Failing request provided no response body."
      }
    }
  }

  suspend fun <R> map(transform: suspend (T) -> R): AtpResponse<R> = when (this) {
    is Success -> Success(transform(response), headers)
    is Failure -> Failure(statusCode, response?.let { transform(it) }, error, headers)
  }
}
