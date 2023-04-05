package sh.christian.ozone.login.auth

data class ServerInfo(
  val inviteCodeRequired: Boolean,
  val availableUserDomains: List<String>,
  val privacyPolicy: String?,
  val termsOfService: String?,
)
