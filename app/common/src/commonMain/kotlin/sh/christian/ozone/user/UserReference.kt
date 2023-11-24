package sh.christian.ozone.user

import sh.christian.ozone.api.Did
import sh.christian.ozone.api.Handle
import kotlin.jvm.JvmInline

sealed interface UserReference

@JvmInline
value class UserDid(
  val did: Did,
) : UserReference {
  override fun toString(): String = did.did
}

@JvmInline
value class UserHandle(
  val handle: Handle,
) : UserReference {
  override fun toString(): String = handle.handle
}
