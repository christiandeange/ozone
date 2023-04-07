package sh.christian.ozone.login

import kotlinx.coroutines.flow.Flow
import sh.christian.ozone.login.auth.AuthInfo
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.getValue
import sh.christian.ozone.store.preference
import sh.christian.ozone.store.setValue

class LoginRepository(
  storage: PersistentStorage,
) {
  private val authPreference = storage.preference<AuthInfo>("auth-info", null)

  var auth by authPreference
  fun auth(): Flow<AuthInfo?> = authPreference.updates
}
