package sh.christian.ozone.api.generator

import java.io.Serializable

sealed interface ApiReturnType : Serializable {
  /** Returns the raw data model type, throwing an exception if the XRPC call failed. */
  object Raw : ApiReturnType

  /** Returns a `Result<T>` wrapping the data model type. */
  object Result : ApiReturnType

  /** Returns an `AtpResponse<T>` wrapping the data model type. */
  object Response : ApiReturnType
}
