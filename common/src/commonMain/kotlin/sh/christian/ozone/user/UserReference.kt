package sh.christian.ozone.user

sealed interface UserReference {
  data class Did(
    val did: String,
  ) : UserReference {
    override fun toString(): String = did
  }

  data class Handle(
    val handle: String,
  ) : UserReference {
    override fun toString(): String = handle
  }
}
