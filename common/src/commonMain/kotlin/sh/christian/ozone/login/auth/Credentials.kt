package sh.christian.ozone.login.auth

data class Credentials(
  val email: String?,
  val username: String,
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
