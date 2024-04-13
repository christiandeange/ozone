package sh.christian.ozone.api.response

/**
 * A sealed type representing the result of performing an XRPC operation.
 */
sealed class AtpResponse<T : Any> {

  /**
   * A successful XRPC operation that returned a valid response.
   */
  data class Success<T : Any>(
    val response: T,
    val headers: Map<String, String>,
  ) : AtpResponse<T>()

  /**
   * A failed XRPC operation that may have returned a valid response, valid error description, or neither.
   */
  data class Failure<T : Any>(
    val statusCode: StatusCode,
    val response: T?,
    val error: AtpErrorDescription?,
    val headers: Map<String, String>,
  ) : AtpResponse<T>()

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

  fun <R : Any> map(transform: (T) -> R): AtpResponse<R> = when (this) {
    is Success -> Success(transform(response), headers)
    is Failure -> Failure(statusCode, response?.let(transform), error, headers)
  }
}
