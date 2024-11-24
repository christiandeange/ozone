package sh.christian.ozone.login.auth

import sh.christian.ozone.util.ReadOnlyList

data class ServerInfo(
  val inviteCodeRequired: Boolean,
  val availableUserDomains: ReadOnlyList<String>,
  val privacyPolicy: String?,
  val termsOfService: String?,
)
