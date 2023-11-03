package sh.christian.ozone.login.auth

import sh.christian.ozone.api.Handle

data class Credentials(
  val email: String?,
  val username: Handle,
  val password: String,
  val inviteCode: String?,
) {
  override fun toString(): String {
    return "Credentials(" +
        "email='$email', " +
        "username='$username', " +
        "password='███', " +
        "inviteCode='$inviteCode'" +
        ")"
  }
}
