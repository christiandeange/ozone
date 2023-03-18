package sh.christian.ozone.error

import sh.christian.ozone.api.response.AtpErrorDescription
import sh.christian.ozone.api.response.AtpResponse

sealed interface ErrorProps {
  val title: String?
  val description: String?
  val retryable: Boolean

  data class AtpError(
    val errorDescription: AtpErrorDescription,
    override val retryable: Boolean
  ) : ErrorProps {
    override val title: String? get() = errorDescription.error
    override val description: String? get() = errorDescription.message
  }

  data class CustomError(
    override val title: String?,
    override val description: String?,
    override val retryable: Boolean,
  ) : ErrorProps
}

fun AtpResponse.Failure<*>.toErrorProps(retryable: Boolean): ErrorProps? =
  error?.let { ErrorProps.AtpError(it, retryable) }
