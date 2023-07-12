package sh.christian.ozone.api.response

import java.io.IOException

class AtpException(
  val statusCode: StatusCode,
) : IOException("XRPC request failed: ${statusCode::class.simpleName}")
