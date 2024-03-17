package sh.christian.ozone.api.response

/**
 * An exception thrown for unsuccessful raw API calls and [Result]-wrapped API calls.
 */
class AtpException(
  val statusCode: StatusCode,
) : Exception("XRPC request failed: ${statusCode::class.simpleName}")
