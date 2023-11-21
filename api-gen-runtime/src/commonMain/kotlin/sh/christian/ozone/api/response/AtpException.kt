package sh.christian.ozone.api.response

class AtpException(
  val statusCode: StatusCode,
) : Exception("XRPC request failed: ${statusCode::class.simpleName}")
