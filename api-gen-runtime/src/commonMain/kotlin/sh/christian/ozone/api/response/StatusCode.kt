package sh.christian.ozone.api.response

/**
 * Represents a status code returned by an HTTP operation.
 */
sealed class StatusCode(val code: Int) {
  /**
   * The request has failed.
   */
  sealed class Failure(code: Int) : StatusCode(code)

  /**
   * The request has failed due to a client error.
   */
  sealed class ClientFailure(code: Int) : Failure(code)

  /**
   * The request has failed due to a server error.
   */
  sealed class ServerFailure(code: Int) : Failure(code)

  /**
   * The request has succeeded.
   */
  data object Okay : StatusCode(200)

  /**
   * The request is invalid and was not processed.
   */
  data object InvalidRequest : ClientFailure(400)

  /**
   * The request cannot be processed without authentication.
   * `WWW-Authenticate` header must be populated with an authentication challenge.
   */
  data object AuthenticationRequired : ClientFailure(401)

  /**
   * The user lacks the needed permissions to access the method.
   */
  data object Forbidden : ClientFailure(403)

  /**
   * The interpretation of a 404 response is somewhat unique for XRPC.
   * A 404 indicates that the server does not provide a resource at the given location (`/xrpc`)
   * meaning the server does not support XRPC.
   * To indicate that the given procedure is not implemented, use [MethodNotImplemented].
   */
  data object XrpcNotSupported : ClientFailure(404)

  /**
   * The payload of the request is larger than the server is willing to process.
   * Payload size-limits are decided by each server.
   */
  data object PayloadTooLarge : ClientFailure(413)

  /**
   * The client has sent too many requests. Rate-limits are decided by each server. `Retry-After`
   * header may be populated with the amount of time that must pass before the next request.
   */
  data object RateLimitExceeded : ClientFailure(429)

  /**
   * The server reached an unexpected condition during processing.
   */
  data object InternalServerError : ServerFailure(500)

  /**
   * The server does not implement the requested method.
   */
  data object MethodNotImplemented : ServerFailure(501)

  /**
   * The execution of the procedure depends on a call to another server which has failed.
   */
  data object UpstreamRequestFailed : ServerFailure(502)

  /**
   * The server is under heavy load and can't complete the request.
   */
  data object NotEnoughResources : ServerFailure(503)

  /**
   * The execution of the procedure depends on a call to another server which timed out.
   */
  data object UpstreamRequestTimeout : ServerFailure(504)

  companion object {
    fun fromCode(code: Int): StatusCode {
      return when (code) {
        // Explicit mappings.
        200 -> Okay
        400 -> InvalidRequest
        401 -> AuthenticationRequired
        403 -> Forbidden
        404 -> XrpcNotSupported
        413 -> PayloadTooLarge
        429 -> RateLimitExceeded
        500 -> InternalServerError
        501 -> MethodNotImplemented
        502 -> UpstreamRequestFailed
        503 -> NotEnoughResources
        504 -> UpstreamRequestTimeout
        // Mappings for ranges.
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
