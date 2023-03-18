package sh.christian.ozone.login

import kotlinx.coroutines.flow.Flow
import sh.christian.ozone.login.auth.AuthInfo
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.preference

class LoginRepository(
  storage: PersistentStorage,
) {
  private val authPreference = storage.preference<AuthInfo?>("auth-info", null)

  var auth by authPreference
  fun auth(): Flow<AuthInfo?> = authPreference.asFlow()
}
