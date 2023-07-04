package sh.christian.ozone.user

sealed interface UserReference {
  data class Did(
    val did: sh.christian.ozone.api.Did,
  ) : UserReference {
    override fun toString(): String = did.did
  }

  data class Handle(
    val handle: sh.christian.ozone.api.Handle,
  ) : UserReference {
    override fun toString(): String = handle.handle
  }
}
