package sh.christian.ozone.login

data class Credentials(
  val username: String,
  val password: String,
) {
  override fun toString(): String {
    return "Credentials(username='$username', password='███')"
  }
}
