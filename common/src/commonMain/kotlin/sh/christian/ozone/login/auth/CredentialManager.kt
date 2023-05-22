package sh.christian.ozone.login.auth

interface CredentialManager {
  suspend fun getStoredCredentials(): Credentials?

  suspend fun saveCredentials(credentials: Credentials)
}
