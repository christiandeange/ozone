package sh.christian.ozone.api.response

sealed class StatusCode(val code: Int) {
  sealed class Failure(code: Int) : StatusCode(code)
  sealed class ClientFailure(code: Int) : Failure(code)
  sealed class ServerFailure(code: Int) : Failure(code)

  /**
   * The request has succeeded.
   **/
  object Okay : StatusCode(200)

  /**
   * The request is invalid and was not processed.
   */
  object InvalidRequest : ClientFailure(400)

  /**
   * The request cannot be processed without authentication.
   * `WWW-Authenticate` header must be populated with an authentication challenge.
   */
  object AuthenticationRequired : ClientFailure(401)

  /**
   * The user lacks the needed permissions to access the method.
   */
  object Forbidden : ClientFailure(403)

  /**
   * The interpretation of a 404 response is somewhat unique for XRPC.
   * A 404 indicates that the server does not provide a resource at the given location (`/xrpc`)
   * meaning the server does not support XRPC.
   * To indicate that the given procedure is not implemented, use [MethodNotImplemented].
   */
  object XrpcNotSupported : ClientFailure(404)

  /**
   * The payload of the request is larger than the server is willing to process.
   * Payload size-limits are decided by each server.
   */
  object PayloadTooLarge : ClientFailure(413)

  /**
   * The client has sent too many requests. Rate-limits are decided by each server. `Retry-After`
   * header may be populated with the amount of time that must pass before the next request.
   */
  object RateLimitExceeded : ClientFailure(429)

  /**
   * The server reached an unexpected condition during processing.
   */
  object InternalServerError : ServerFailure(500)

  /**
   * The server does not implement the requested method.
   */
  object MethodNotImplemented : ServerFailure(501)

  /**
   * The execution of the procedure depends on a call to another server which has failed.
   */
  object UpstreamRequestFailed : ServerFailure(502)

  /**
   * The server is under heavy load and can't complete the request.
   */
  object NotEnoughResources : ServerFailure(503)

  /**
   * The execution of the procedure depends on a call to another server which timed out.
   */
  object UpstreamRequestTimeout : ServerFailure(504)

  companion object {
    fun values(): List<StatusCode> {
      return StatusCode::class.nestedClasses
        .mapNotNull { it.objectInstance }
        .filterIsInstance<StatusCode>()
    }

    fun fromCode(code: Int): StatusCode {
      val codeMapping = values().associateBy { it.code }

      return when (code) {
        in codeMapping.keys -> codeMapping[code]!!
        in 100..199 -> XrpcNotSupported
        in 200..299 -> Okay
        in 300..399 -> XrpcNotSupported
        in 400..499 -> InvalidRequest
        in 500..599 -> InternalServerError
        else -> error("Unknown status code returned: $code")
      }
    }
  }
}
